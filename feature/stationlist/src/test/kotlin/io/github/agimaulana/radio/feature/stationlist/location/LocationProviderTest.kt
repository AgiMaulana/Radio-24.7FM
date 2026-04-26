package io.github.agimaulana.radio.feature.stationlist.location

import android.content.Context
import android.location.Location
import androidx.test.core.app.ApplicationProvider
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
    fun `getHighAccuracyLocation returns location when fused client provides result`() = runTest {
        val fused = mockk<FusedLocationProviderClient>()
        val expected = Location("gps").apply { latitude = -6.2; longitude = 106.8 }

        every { fused.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, any()) } returns Tasks.forResult(expected)

        val provider = LocationProvider(context, fused, highAccuracyTimeoutMs = 5_000L)

        val actual = provider.getHighAccuracyLocation()

        assertNotNull(actual)
        assertEquals(expected.latitude, actual!!.latitude, 0.0)
        assertEquals(expected.longitude, actual.longitude, 0.0)
    }

    @Test
    fun `getCurrentLocation falls back to balanced when high fails`() = runTest {
        val fused = mockk<FusedLocationProviderClient>()
        val balanced = Location("network").apply { latitude = 1.23; longitude = 4.56 }

        every { fused.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, any()) } returns Tasks.forException(Exception("no high"))
        every { fused.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, any()) } returns Tasks.forResult(balanced)

        val provider = LocationProvider(context, fused, highAccuracyTimeoutMs = 1L)

        val high = provider.getHighAccuracyLocation()
        assertNull(high)

        val bal = provider.getBalancedPowerAccuracyLocation()
        assertNotNull(bal)
        assertEquals(balanced.latitude, bal!!.latitude, 0.0)
    }
}

