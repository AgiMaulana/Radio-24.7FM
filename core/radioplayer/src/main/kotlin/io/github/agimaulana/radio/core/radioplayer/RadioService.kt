package io.github.agimaulana.radio.core.radioplayer

import android.app.PendingIntent
import android.content.Intent
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultLivePlaybackSpeedControl
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import io.github.agimaulana.radio.core.radioplayer.internal.PlaybackManager
import io.github.agimaulana.radio.core.radioplayer.internal.RadioMediaSessionCallback
import io.github.agimaulana.radio.core.radioplayer.internal.ServiceResolver

@OptIn(UnstableApi::class)
class RadioService : MediaSessionService() {
    private var mediaSession: MediaSession? = null
    private var playbackManager: PlaybackManager? = null

    override fun onCreate() {
        super.onCreate()
        val player = createPlayer()
        // Initialize PlaybackManager with a no-op fetcher for now. The fetcher will be injected/wired later
        // Wire a fetcher that calls the domain use-case via a lazy resolver to avoid tight coupling in module boundaries.
        playbackManager = PlaybackManager(player) { page, query ->
            val resolver = ServiceResolver.resolveGetRadioStationsResolver()
            if (resolver != null) {
                try {
                    resolver.invoke(page, query)
                } catch (e: Exception) {
                    if (e is kotlinx.coroutines.CancellationException) {
                        throw e
                    }
                    emptyList()
                }
            } else emptyList()
        }

        // Register PlaybackManager.startPlayback with ServiceResolver so controller can call it
        playbackManager?.let { manager ->
            ServiceResolver.registerPlaybackStartCallback { items, startIndex, contextType, contextQuery ->
                val type = contextType?.let { ct ->
                    runCatching { PlaybackManager.PlaybackContext.Type.valueOf(ct) }
                        .getOrElse { PlaybackManager.PlaybackContext.Type.DEFAULT }
                } ?: PlaybackManager.PlaybackContext.Type.DEFAULT
                manager.startPlayback(
                    items,
                    startIndex,
                    PlaybackManager.PlaybackContext(type = type, query = contextQuery)
                )
            }
        }

        mediaSession = MediaSession.Builder(this, player)
            .setSessionActivity(createPendingMainActivityIntent())
            .setCallback(RadioMediaSessionCallback())
            .build()
        setMediaNotificationProvider(DefaultMediaNotificationProvider.Builder(this).build())

        // Attempt to restore playback context from player playlist (if any persisted metadata exists)
        playbackManager?.restoreFromPlayer()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player
        if (player?.playWhenReady == false || player?.mediaItemCount == 0) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        playbackManager?.release()
        super.onDestroy()
    }

    private fun createPlayer(): ExoPlayer {
        return ExoPlayer.Builder(this)
            .setLoadControl(DefaultLoadControl())
            .setLivePlaybackSpeedControl(DefaultLivePlaybackSpeedControl.Builder().build())
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .setUsage(C.USAGE_MEDIA)
                    .build(),
                true
            )
            .build()
    }

    private fun createPendingMainActivityIntent(): PendingIntent {
        return PendingIntent.getActivity(
            this,
            0,
            packageManager.getLaunchIntentForPackage(packageName),
            PendingIntent.FLAG_IMMUTABLE
        )
    }

}
