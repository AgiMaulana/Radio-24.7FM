package io.github.agimaulana.radio.core.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
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

    enum class Accuracy {
        COARSE,
        FINE
    }

    suspend fun checkLocationSettings(
        priority: Int = Priority.PRIORITY_HIGH_ACCURACY
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
    suspend fun getLocation(accuracy: Accuracy): LocationInfo? {
        val priority = when (accuracy) {
            Accuracy.COARSE -> Priority.PRIORITY_BALANCED_POWER_ACCURACY
            Accuracy.FINE -> Priority.PRIORITY_HIGH_ACCURACY
        }

        val location = getCurrentLocation(priority)
            ?: if (accuracy == Accuracy.COARSE) getLastKnownLocation() else null

        if (location == null) return null

        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            
            @Suppress("DEPRECATION")
            val addresses = withContext(Dispatchers.IO) {
                geocoder.getFromLocation(location.latitude, location.longitude, 1)
            }
            val address = addresses?.firstOrNull()
            
            LocationInfo(
                city = address?.locality ?: address?.subAdminArea ?: "",
                adminArea = address?.adminArea ?: "",
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

    @SuppressLint("MissingPermission")
    private suspend fun getCurrentLocation(priority: Int): Location? {
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

    @SuppressLint("MissingPermission")
    private suspend fun getLastKnownLocation(): Location? {
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
