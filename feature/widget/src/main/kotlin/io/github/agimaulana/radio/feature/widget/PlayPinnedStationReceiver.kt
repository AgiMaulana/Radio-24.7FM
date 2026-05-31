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
        when (intent?.action) {
            ACTION_PLAY_PINNED -> handlePlay(intent)
            ACTION_PAUSE_PINNED -> handlePause()
        }
    }

    private fun handlePlay(intent: Intent) {
        val mediaId = intent.getStringExtra("extra_media_id") ?: return
        val pendingResult = goAsync()

        scope.launch {
            try {
                val browser = radioBrowserFactory.get()
                val station = browser.getStation(mediaId)
                browser.release()

                if (station != null) {
                    val player = radioPlayerControllerFactory.get()
                    player.startPlayback(
                        items = listOf(station),
                        startIndex = 0,
                        context = RadioPlayerController.PlaybackContext(
                            type = RadioPlayerController.PlaybackContext.Type.PINNED
                        )
                    )
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

    private fun handlePause() {
        val pendingResult = goAsync()

        scope.launch {
            try {
                val player = radioPlayerControllerFactory.get()
                player.pause()
            } catch (t: Throwable) {
                Timber.e(t, "Failed to pause pinned station: %s", t.localizedMessage)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION_PLAY_PINNED = "io.github.agimaulana.radio.action.PLAY_PINNED"
        const val ACTION_PAUSE_PINNED = "io.github.agimaulana.radio.action.PAUSE_PINNED"
    }
}
