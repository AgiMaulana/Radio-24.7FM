package io.github.agimaulana.radio.core.radioplayer

import android.content.ComponentName
import android.content.Context
import androidx.media3.session.MediaBrowser
import androidx.media3.session.SessionToken
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.agimaulana.radio.core.radioplayer.internal.RadioBrowserControllerImpl
import io.github.agimaulana.radio.domain.api.repository.PinnedStationRepository
import kotlinx.coroutines.guava.await
import javax.inject.Inject

class RadioBrowserFactory @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val pinnedStationRepository: PinnedStationRepository,
) {
    suspend fun get(): RadioBrowserController {
        val controller = RadioBrowserControllerImpl(pinnedStationRepository.maxPins)
        val sessionToken = SessionToken(context, ComponentName(context, RadioService::class.java))
        val browser = MediaBrowser.Builder(context, sessionToken)
            .setListener(controller.browserListener)
            .buildAsync()
            .await()

        controller.attach(browser)
        return controller
    }
}
