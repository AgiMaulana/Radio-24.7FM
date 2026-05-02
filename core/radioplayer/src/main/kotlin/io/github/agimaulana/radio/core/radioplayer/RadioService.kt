package io.github.agimaulana.radio.core.radioplayer

import android.app.PendingIntent
import android.content.Intent
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultLivePlaybackSpeedControl
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import dagger.hilt.android.AndroidEntryPoint
import io.github.agimaulana.radio.core.common.WidgetConstants
import io.github.agimaulana.radio.core.radioplayer.internal.PlaybackManager
import io.github.agimaulana.radio.core.radioplayer.internal.RadioMediaSessionCallback
import io.github.agimaulana.radio.core.radioplayer.internal.ServiceResolver
import io.github.agimaulana.radio.domain.api.usecase.GetRadioStationsUseCase
import javax.inject.Inject

@OptIn(UnstableApi::class)
@AndroidEntryPoint
class RadioService : MediaSessionService() {
    @Inject lateinit var getRadioStationsUseCase: GetRadioStationsUseCase

    private var mediaSession: MediaSession? = null
    private var playbackManager: PlaybackManager? = null

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            sendWidgetRefreshBroadcast()
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            sendWidgetRefreshBroadcast()
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            sendWidgetRefreshBroadcast()
        }
    }

    override fun onCreate() {
        super.onCreate()
        val player = createPlayer()
        player.addListener(playerListener)

        val fetcher: suspend (Int, String?) -> List<RadioMediaItem> = { page, query ->
            getRadioStationsUseCase.execute(page = page, searchName = query, location = null)
                .map { station ->
                    RadioMediaItem(
                        mediaId = station.stationUuid,
                        streamUrl = station.resolvedUrl.ifEmpty { station.url },
                        radioMetadata = RadioMediaItem.RadioMetadata(
                            stationName = station.name,
                            genre = station.tags.firstOrNull() ?: "",
                            imageUrl = station.imageUrl
                        )
                    )
                }
        }
        playbackManager = PlaybackManager(player, fetcher)

        ServiceResolver.registerPlaybackStartCallback { items, startIndex, contextType, contextQuery ->
            val type = contextType?.let { ct ->
                runCatching { PlaybackManager.PlaybackContext.Type.valueOf(ct) }
                    .getOrElse { PlaybackManager.PlaybackContext.Type.DEFAULT }
            } ?: PlaybackManager.PlaybackContext.Type.DEFAULT
            playbackManager?.startPlayback(
                items,
                startIndex,
                PlaybackManager.PlaybackContext(type = type, query = contextQuery)
            )
        }

        mediaSession = MediaSession.Builder(this, player)
            .setSessionActivity(createPendingMainActivityIntent())
            .setCallback(RadioMediaSessionCallback())
            .build()
        setMediaNotificationProvider(DefaultMediaNotificationProvider.Builder(this).build())

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
            player.removeListener(playerListener)
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

    private fun sendWidgetRefreshBroadcast() {
        val intent = Intent(WidgetConstants.ACTION_REFRESH_WIDGETS).apply {
            setPackage(packageName)
        }
        sendBroadcast(intent)
    }
}
