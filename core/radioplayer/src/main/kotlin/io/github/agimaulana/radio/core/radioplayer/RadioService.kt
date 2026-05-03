package io.github.agimaulana.radio.core.radioplayer

import android.app.PendingIntent
import android.content.Intent
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultLivePlaybackSpeedControl
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import dagger.hilt.android.AndroidEntryPoint
import io.github.agimaulana.radio.core.radioplayer.internal.RadioLibraryCatalog
import io.github.agimaulana.radio.core.radioplayer.internal.RadioSessionCallback
import io.github.agimaulana.radio.domain.api.repository.CatalogStateRepository
import io.github.agimaulana.radio.domain.api.usecase.GetRadioStationsUseCase
import javax.inject.Inject

@OptIn(UnstableApi::class)
@AndroidEntryPoint
class RadioService : MediaLibraryService() {
    @Inject lateinit var getRadioStationsUseCase: GetRadioStationsUseCase
    @Inject lateinit var catalogStateRepository: CatalogStateRepository

    private var mediaSession: MediaLibraryService.MediaLibrarySession? = null
    private lateinit var radioLibraryCatalog: RadioLibraryCatalog
    private lateinit var radioSessionCallback: RadioSessionCallback

    override fun onCreate() {
        super.onCreate()
        val player = createPlayer()
        radioLibraryCatalog = RadioLibraryCatalog(getRadioStationsUseCase, catalogStateRepository)
        radioSessionCallback = RadioSessionCallback(radioLibraryCatalog)

        mediaSession = MediaLibraryService.MediaLibrarySession.Builder(this, player, radioSessionCallback)
            .setSessionActivity(createPendingMainActivityIntent())
            .build()
        setMediaNotificationProvider(DefaultMediaNotificationProvider.Builder(this).build())
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibraryService.MediaLibrarySession? {
        return mediaSession
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player
        if (
            player == null ||
            (player.mediaItemCount == 0 && player.playbackState == Player.STATE_IDLE)
        ) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        if (::radioSessionCallback.isInitialized) {
            radioSessionCallback.close()
        }
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
