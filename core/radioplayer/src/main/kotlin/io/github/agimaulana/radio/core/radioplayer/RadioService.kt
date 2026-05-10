package io.github.agimaulana.radio.core.radioplayer

import android.app.PendingIntent
import android.content.Intent
import androidx.annotation.OptIn
import androidx.media3.cast.CastPlayer
import androidx.media3.cast.MediaItemConverter
import androidx.media3.cast.RemoteCastPlayer
import androidx.media3.cast.SessionAvailabilityListener
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultLivePlaybackSpeedControl
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaQueueItem
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.common.images.WebImage
import com.google.common.util.concurrent.MoreExecutors
import dagger.hilt.android.AndroidEntryPoint
import io.github.agimaulana.radio.core.radioplayer.internal.PlaylistPaginator
import io.github.agimaulana.radio.core.radioplayer.internal.RadioLibraryCatalog
import io.github.agimaulana.radio.core.radioplayer.internal.RadioSessionCallback
import io.github.agimaulana.radio.domain.api.repository.CatalogStateRepository
import io.github.agimaulana.radio.domain.api.usecase.GetRadioStationUseCase
import io.github.agimaulana.radio.domain.api.usecase.GetRadioStationsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import timber.log.Timber
import javax.inject.Inject

@OptIn(UnstableApi::class)
@AndroidEntryPoint
class RadioService : MediaLibraryService() {
    @Inject lateinit var getRadioStationsUseCase: GetRadioStationsUseCase
    @Inject lateinit var getRadioStationUseCase: GetRadioStationUseCase
    @Inject lateinit var catalogStateRepository: CatalogStateRepository

    private var mediaSession: MediaLibrarySession? = null
    private lateinit var radioLibraryCatalog: RadioLibraryCatalog
    private lateinit var radioSessionCallback: RadioSessionCallback
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var playlistPaginator: PlaylistPaginator? = null

    private var player: Player? = null

    override fun onCreate() {
        super.onCreate()
        radioLibraryCatalog = RadioLibraryCatalog(
            getRadioStationsUseCase,
            getRadioStationUseCase,
            catalogStateRepository
        )
        radioSessionCallback = RadioSessionCallback(radioLibraryCatalog)

        initializePlayer()
    }

    private fun initializePlayer() {
        val exoPlayer = createExoPlayer()
        val castPlayerBuilder = CastPlayer.Builder(this)
            .setLocalPlayer(exoPlayer)
            .setTransferCallback { sourcePlayer, targetPlayer ->
                val items = mutableListOf<MediaItem>()
                for (i in 0 until sourcePlayer.mediaItemCount) {
                    items.add(sourcePlayer.getMediaItemAt(i))
                }
                val currentIndex = sourcePlayer.currentMediaItemIndex
                val position = sourcePlayer.currentPosition
                targetPlayer.setMediaItems(items, currentIndex, position)
                targetPlayer.playWhenReady = sourcePlayer.playWhenReady
                targetPlayer.prepare()
            }

        try {
            val mediaItemConverter = object : MediaItemConverter {
                override fun toMediaQueueItem(mediaItem: MediaItem): MediaQueueItem {
                    return mediaItem.toMediaQueueItem()
                }

                override fun toMediaItem(mediaQueueItem: MediaQueueItem): MediaItem {
                    return mediaQueueItem.toMediaItem()
                }
            }
            CastContext.getSharedInstance(this, MoreExecutors.directExecutor())
                .addOnSuccessListener {
                    val remotePlayer = RemoteCastPlayer.Builder(this)
                        .setMediaItemConverter(mediaItemConverter)
                        .build()
                    remotePlayer.setSessionAvailabilityListener(object : SessionAvailabilityListener {
                        override fun onCastSessionAvailable() {
                            player?.let { playlistPaginator?.updatePlayer(it) }
                        }

                        override fun onCastSessionUnavailable() {
                            player?.let { playlistPaginator?.updatePlayer(it) }
                        }
                    })
                    val castPlayer = castPlayerBuilder.setRemotePlayer(remotePlayer)
                        .setLocalPlayer(exoPlayer)
                        .build()
                    setupSession(castPlayer)
                }
                .addOnFailureListener { e ->
                    Timber.tag("RadioService").e(e, "CastContext init failed, using ExoPlayer only")
                    setupSession(exoPlayer)
                }
        } catch (e: RuntimeException) {
            Timber.tag("RadioService").w(e, "Cast unavailable")
            setupSession(exoPlayer)
        }
    }

    private fun setupSession(player: Player) {
        this.player = player
        playlistPaginator = PlaylistPaginator(player, radioLibraryCatalog, serviceScope)

        mediaSession = MediaLibrarySession.Builder(this, player, radioSessionCallback)
            .setSessionActivity(createPendingMainActivityIntent())
            .build()
        setMediaNotificationProvider(DefaultMediaNotificationProvider.Builder(this).build())
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession? {
        return mediaSession
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player
        if (
            player == null ||
            (player.mediaItemCount == 0 && (player.playbackState == Player.STATE_IDLE))
        ) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        playlistPaginator?.release()
        playlistPaginator = null
        
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

    private fun createExoPlayer(): ExoPlayer {
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

    private fun MediaItem.toMediaQueueItem(): MediaQueueItem {
        val metadata = this.mediaMetadata

        val castMetadata = com.google.android.gms.cast.MediaMetadata(
            com.google.android.gms.cast.MediaMetadata.MEDIA_TYPE_MUSIC_TRACK
        ).apply {
            putString(com.google.android.gms.cast.MediaMetadata.KEY_TITLE, metadata.title?.toString() ?: "Station")
            putString(com.google.android.gms.cast.MediaMetadata.KEY_SUBTITLE, metadata.subtitle?.toString().orEmpty())
            putString(com.google.android.gms.cast.MediaMetadata.KEY_ARTIST, (metadata.artist ?: metadata.subtitle)?.toString().orEmpty())
            putString(com.google.android.gms.cast.MediaMetadata.KEY_ALBUM_TITLE, metadata.albumTitle?.toString() ?: "Radio")
            metadata.artworkUri?.let { addImage(WebImage(it)) }
        }

        val uriString = this.localConfiguration?.uri?.toString() ?: this.requestMetadata.mediaUri?.toString()
        if (uriString.isNullOrEmpty() || uriString == "null") {
            Timber.tag("RadioService").w("Cannot convert MediaItem to MediaQueueItem: No URI for %s", metadata.title)
            // Return a minimal item to avoid crashing, though this item will likely fail to play
            return MediaQueueItem.Builder(MediaInfo.Builder("invalid://").build()).build()
        }

        val mediaInfo = MediaInfo.Builder(uriString)
            .setStreamType(MediaInfo.STREAM_TYPE_LIVE)
            .setStreamDuration(MediaInfo.UNKNOWN_DURATION)
            .setContentType(this.localConfiguration?.mimeType ?: "audio/mpeg")
            .setMetadata(castMetadata)
            .build()

        return MediaQueueItem.Builder(mediaInfo)
            .setAutoplay(true)
            .build()
    }

    private fun MediaQueueItem.toMediaItem(): MediaItem {
        val castMetadata = this.media?.metadata

        return MediaItem.Builder()
            .setUri(this.media?.contentId)
            .setMimeType(this.media?.contentType)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(castMetadata?.getString(com.google.android.gms.cast.MediaMetadata.KEY_TITLE))
                    .setSubtitle(castMetadata?.getString(com.google.android.gms.cast.MediaMetadata.KEY_SUBTITLE))
                    .setArtist(castMetadata?.getString(com.google.android.gms.cast.MediaMetadata.KEY_ARTIST))
                    .setArtworkUri(castMetadata?.images?.firstOrNull()?.url)
                    .build()
            )
            .build()
    }
}
