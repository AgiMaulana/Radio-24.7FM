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
import io.github.agimaulana.radio.domain.api.usecase.GetRadioStationUseCase
import io.github.agimaulana.radio.domain.api.usecase.GetRadioStationsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(UnstableApi::class)
@AndroidEntryPoint
class RadioService : MediaLibraryService() {
    @Inject lateinit var getRadioStationsUseCase: GetRadioStationsUseCase
    @Inject lateinit var getRadioStationUseCase: GetRadioStationUseCase
    @Inject lateinit var catalogStateRepository: CatalogStateRepository

    private var mediaSession: MediaLibraryService.MediaLibrarySession? = null
    private lateinit var radioLibraryCatalog: RadioLibraryCatalog
    private lateinit var radioSessionCallback: RadioSessionCallback
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var isLoadingNextPage = false

    override fun onCreate() {
        super.onCreate()
        radioLibraryCatalog = RadioLibraryCatalog(
            getRadioStationsUseCase,
            getRadioStationUseCase,
            catalogStateRepository
        )
        val player = createPlayer()
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
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun createPlayer(): ExoPlayer {
        val player = ExoPlayer.Builder(this)
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

        player.addListener(object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: androidx.media3.common.MediaItem?, reason: Int) {
                if (mediaItem == null) return
                val currentIndex = player.currentMediaItemIndex
                val playlistSize = player.mediaItemCount
                if (!isLoadingNextPage && currentIndex >= playlistSize - 2 && playlistSize > 0) {
                    isLoadingNextPage = true
                    serviceScope.launch {
                        try {
                            val nextPage = (playlistSize / RadioLibraryCatalog.CATALOG_PAGE_SIZE)
                            val newItems = radioLibraryCatalog.loadChildren(
                                nextPage,
                                RadioLibraryCatalog.CATALOG_PAGE_SIZE
                            )
                            if (newItems.isNotEmpty()) {
                                val currentIds = (0 until player.mediaItemCount)
                                    .map { player.getMediaItemAt(it).mediaId }.toSet()
                                val filteredNewItems = newItems.filter { it.mediaId !in currentIds }
                                if (filteredNewItems.isNotEmpty()) {
                                    player.addMediaItems(filteredNewItems)
                                }
                            }
                        } finally {
                            isLoadingNextPage = false
                        }
                    }
                }
            }
        })
        return player
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
