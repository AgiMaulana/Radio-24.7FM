package io.github.agimaulana.radio.feature.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import io.github.agimaulana.radio.core.radioplayer.RadioBrowserFactory
import io.github.agimaulana.radio.core.radioplayer.RadioPlayerController
import io.github.agimaulana.radio.core.radioplayer.RadioPlayerControllerFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class PlayPinnedStationReceiver : BroadcastReceiver() {

    @Inject lateinit var radioBrowserFactory: RadioBrowserFactory
    @Inject lateinit var radioPlayerControllerFactory: RadioPlayerControllerFactory

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onReceive(context: Context, intent: Intent?) {
        val mediaId = intent?.getStringExtra("extra_media_id") ?: return
        val pendingResult = goAsync()

        scope.launch {
            try {
                // Get station details
                val browser = radioBrowserFactory.get()
                val station = browser.getStation(mediaId)
                browser.release()

                if (station != null) {
                    // Play immediately via controller
                    val player = radioPlayerControllerFactory.get()
                    player.startPlayback(
                        items = listOf(station),
                        startIndex = 0,
                        context = RadioPlayerController.PlaybackContext(
                            type = RadioPlayerController.PlaybackContext.Type.PINNED
                        )
                    )
                    // We don't release player here because it's managing the session
                } else {
                    Timber.e("Station not found: %s", mediaId)
                }
            } catch (t: Throwable) {
                Timber.e(t, "Failed to play pinned station: %s", t.localizedMessage)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
