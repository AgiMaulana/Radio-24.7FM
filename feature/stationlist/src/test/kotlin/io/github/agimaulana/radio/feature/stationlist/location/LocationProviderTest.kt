package io.github.agimaulana.radio.feature.stationlist.location

import android.content.Context
import android.location.Location
import androidx.test.core.app.ApplicationProvider
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.Priority
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.tasks.Tasks
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import org.robolectric.RobolectricTestRunner
import org.junit.runner.RunWith

@RunWith(RobolectricTestRunner::class)
class LocationProviderTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun `getCurrentLocation returns locationInfo when fused client provides result`() = runTest {
        val fused = mockk<FusedLocationProviderClient>()
        val expected = Location("gps").apply { latitude = -6.2; longitude = 106.8 }

        every { fused.getCurrentLocation(any(CurrentLocationRequest::class), any()) } returns Tasks.forResult(expected)

        val provider = LocationProvider(context, fused, highAccuracyTimeoutMs = 5_000L)

        val actual = provider.getCurrentLocation()

        assertNotNull(actual)
        assertEquals(expected.latitude, actual!!.latitude, 0.0)
        assertEquals(expected.longitude, actual.longitude, 0.0)
    }

    @Test
    fun `getCurrentLocation falls back to balanced when high fails`() = runTest {
        val fused = mockk<FusedLocationProviderClient>()
        val balanced = Location("network").apply { latitude = 1.23; longitude = 4.56 }

        every { fused.getCurrentLocation(any(CurrentLocationRequest::class), any()) } returnsMany listOf(
            Tasks.forException<Location>(Exception("no high")),
            Tasks.forResult(balanced),
            Tasks.forResult(balanced)
        )

        val provider = LocationProvider(context, fused, highAccuracyTimeoutMs = 1L)

        val result = provider.getCurrentLocation()
        assertNotNull(result)
        assertEquals(balanced.latitude, result!!.latitude, 0.0)
        assertEquals(balanced.longitude, result.longitude, 0.0)
    }
}

