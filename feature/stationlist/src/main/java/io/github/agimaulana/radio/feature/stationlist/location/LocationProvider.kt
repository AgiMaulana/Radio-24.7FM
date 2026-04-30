package io.github.agimaulana.radio.feature.stationlist.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber
import java.util.Locale
import javax.inject.Inject
import javax.inject.Named

class LocationProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context),
    private val settingsClient: SettingsClient = LocationServices.getSettingsClient(context),
    @Named("highAccuracyTimeoutMs") private val highAccuracyTimeoutMs: Long = 5_000L
) {

    /**
     * Checks if location services (GPS or Network) are enabled on the device.
     */
    fun isLocationEnabled(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    /**
     * Checks if GPS is enabled on the device.
     */
    fun isGpsEnabled(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    /**
     * Checks if the current location settings satisfy the requirement for the given priority.
     * Defaults to [Priority.PRIORITY_BALANCED_POWER_ACCURACY] to allow coarse location
     * without forcing GPS to be enabled.
     *
     * Returns a [Result] which may contain a [com.google.android.gms.common.api.ResolvableApiException]
     * if the settings can be resolved by the user.
     */
    suspend fun checkLocationSettings(
        priority: Int = Priority.PRIORITY_BALANCED_POWER_ACCURACY
    ): Result<Unit> {
        val locationRequest = LocationRequest.Builder(priority, 1000).build()
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        
        return try {
            settingsClient.checkLocationSettings(builder.build()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): LocationInfo? {
        if (!isLocationEnabled()) {
            Timber.d("Location is disabled on device")
            return null
        }

        val priority = if (isGpsEnabled()) {
            Priority.PRIORITY_HIGH_ACCURACY
        } else {
            Priority.PRIORITY_BALANCED_POWER_ACCURACY
        }

        // Prefer a small chain of progressively less demanding providers.
        // If GPS is disabled, start from balanced accuracy to keep coarse location working.
        val location = getCurrentLocation(priority)
            ?: getBalancedPowerAccuracyLocation()
            ?: getLowPowerLocation()
            ?: getLastKnownLocation()

        if (location == null) return null

        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            
            @Suppress("DEPRECATION")
            val addresses = withContext(Dispatchers.IO) {
                geocoder.getFromLocation(location.latitude, location.longitude, 1)
            }
            val address = addresses?.firstOrNull()
            
            LocationInfo(
                city = address?.adminArea ?: "",
                adminArea = address?.locality ?: address?.subAdminArea ?: "",
                country = address?.countryName ?: "",
                latitude = location.latitude,
                longitude = location.longitude
            )
        } catch (e: Exception) {
            Timber.e(e, "Failed to get location info")
            LocationInfo(
                city = "",
                adminArea = "",
                country = "",
                latitude = location.latitude,
                longitude = location.longitude
            )
        }
    }

    // Helper: try high accuracy (GPS)
    @SuppressLint("MissingPermission")
    internal suspend fun getCurrentLocation(priority: Int): Location? {
        return try {
            val cts = CancellationTokenSource()
            try {
                val request = CurrentLocationRequest.Builder()
                    .setPriority(priority)
                    .setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
                    .build()

                withTimeoutOrNull(highAccuracyTimeoutMs) {
                    fusedLocationClient.getCurrentLocation(request, cts.token).await()
                }
            } finally {
                cts.cancel()
            }
        } catch (e: Exception) {
            Timber.d(e, "current location failed for priority=%s", priority)
            null
        }
    }

    // Helper: try balanced power accuracy (network/GPS hybrid)
    @SuppressLint("MissingPermission")
    internal suspend fun getBalancedPowerAccuracyLocation(): Location? =
        getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY)

    // Helper: try low power (may use cached or coarse sources)
    @SuppressLint("MissingPermission")
    internal suspend fun getLowPowerLocation(): Location? =
        getCurrentLocation(Priority.PRIORITY_LOW_POWER)

    // Helper: fall back to last known location (may be stale but better than nothing)
    @SuppressLint("MissingPermission")
    internal suspend fun getLastKnownLocation(): Location? {
        return try {
            fusedLocationClient.lastLocation.await()
        } catch (e: Exception) {
            Timber.d(e, "last-known failed")
            null
        }
    }

    data class LocationInfo(
        val city: String,
        val adminArea: String,
        val country: String,
        val latitude: Double,
        val longitude: Double
    )
}
