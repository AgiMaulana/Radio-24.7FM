package io.github.agimaulana.radio.feature.stationlist.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.util.Locale
import javax.inject.Inject

class LocationProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): LocationInfo? {
        val location = try {
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                CancellationTokenSource().token
            ).await()
        } catch (e: Exception) {
            Timber.e(e, "Failed to last location")
            null
        } ?: return null

        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            
            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
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
