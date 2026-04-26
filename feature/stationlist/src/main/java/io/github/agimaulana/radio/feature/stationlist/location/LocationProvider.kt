package io.github.agimaulana.radio.feature.stationlist.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.Locale
import javax.inject.Inject

class LocationProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

@SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): LocationInfo? {
        // Try high accuracy first, then fall back to balanced power accuracy if needed.
        var location = try {
            val cts = CancellationTokenSource()
            try {
                fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    cts.token
                ).await()
            } finally {
                cts.cancel()
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to get high-accuracy location")
            null
        }

        if (location == null) {
            // Fallback to balanced power accuracy
            location = try {
                val cts2 = CancellationTokenSource()
                try {
                    fusedLocationClient.getCurrentLocation(
                        Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                        cts2.token
                    ).await()
                } finally {
                    cts2.cancel()
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to get fallback location")
                null
            }
        }

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
                country = address?.countryName ?: "",
                latitude = location.latitude,
                longitude = location.longitude
            )
        } catch (e: Exception) {
            Timber.e(e, "Failed to get location info")
            LocationInfo(
                city = "",
                country = "",
                latitude = location.latitude,
                longitude = location.longitude
            )
        }
    }

    data class LocationInfo(
        val city: String,
        val country: String,
        val latitude: Double,
        val longitude: Double
    )
}
