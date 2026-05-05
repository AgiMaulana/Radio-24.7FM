package io.github.agimaulana.radio.core.radioplayer

import android.app.PendingIntent
import android.content.Intent
import androidx.annotation.OptIn
import androidx.media3.cast.CastPlayer
import androidx.media3.cast.RemoteCastPlayer
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
import com.google.android.gms.cast.framework.CastContext
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
        val castPlayerBuilder = CastPlayer.Builder(this).setLocalPlayer(exoPlayer)

        try {
            CastContext.getSharedInstance(this, MoreExecutors.directExecutor())
                .addOnSuccessListener {
                    val remotePlayer = RemoteCastPlayer.Builder(this).build()
                    val castPlayer = castPlayerBuilder.setRemotePlayer(remotePlayer).build()
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
}
