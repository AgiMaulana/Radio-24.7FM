package io.github.agimaulana.radio.core.radioplayer

import android.app.PendingIntent
import android.content.Intent
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import androidx.media3.exoplayer.DefaultLivePlaybackSpeedControl
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import dagger.hilt.android.AndroidEntryPoint
import io.github.agimaulana.radio.core.radioplayer.internal.RadioLibraryCatalog
import io.github.agimaulana.radio.domain.api.usecase.GetRadioStationsUseCase
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@OptIn(UnstableApi::class)
@AndroidEntryPoint
class RadioService : MediaLibraryService() {
    @Inject lateinit var getRadioStationsUseCase: GetRadioStationsUseCase

    private var mediaSession: MediaLibraryService.MediaLibrarySession? = null
    private lateinit var radioLibraryCatalog: RadioLibraryCatalog

    override fun onCreate() {
        super.onCreate()
        val player = createPlayer()
        radioLibraryCatalog = RadioLibraryCatalog(getRadioStationsUseCase)

        mediaSession = MediaLibraryService.MediaLibrarySession.Builder(this, player, RadioLibraryCallback())
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

    private inner class RadioLibraryCallback : MediaLibraryService.MediaLibrarySession.Callback {
        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
        ): MediaSession.ConnectionResult {
            val connectionResult = super.onConnect(session, controller)

            @OptIn(UnstableApi::class)
            val customPlayerCommands = connectionResult.availablePlayerCommands.buildUpon()
                .add(Player.COMMAND_SEEK_TO_NEXT)
                .add(Player.COMMAND_SEEK_TO_PREVIOUS)
                .add(Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM)
                .add(Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM)
                .build()

            return MediaSession.ConnectionResult.accept(
                connectionResult.availableSessionCommands,
                customPlayerCommands
            )
        }

        override fun onGetLibraryRoot(
            session: MediaLibraryService.MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            params: MediaLibraryService.LibraryParams?
        ): com.google.common.util.concurrent.ListenableFuture<LibraryResult<MediaItem>> {
            return Futures.immediateFuture(LibraryResult.ofItem(radioLibraryCatalog.rootItem(), params))
        }

        override fun onGetChildren(
            session: MediaLibraryService.MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            parentId: String,
            page: Int,
            pageSize: Int,
            params: MediaLibraryService.LibraryParams?
        ): com.google.common.util.concurrent.ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
            if (parentId != RadioLibraryCatalog.ROOT_MEDIA_ID) {
                return Futures.immediateFuture(LibraryResult.ofItemList(emptyList(), params))
            }

            val stations = runBlocking {
                radioLibraryCatalog.loadChildren()
            }

            return Futures.immediateFuture(LibraryResult.ofItemList(stations, params))
        }
    }
}
