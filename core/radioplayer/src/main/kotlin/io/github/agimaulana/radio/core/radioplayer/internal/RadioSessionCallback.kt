package io.github.agimaulana.radio.core.radioplayer.internal

import android.content.Intent
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import kotlinx.coroutines.runBlocking
import timber.log.Timber

@OptIn(UnstableApi::class)
internal class RadioSessionCallback(
    private val radioLibraryCatalog: RadioLibraryCatalog,
) : MediaLibraryService.MediaLibrarySession.Callback {

    override fun onConnect(
        session: MediaSession,
        controller: MediaSession.ControllerInfo
    ): MediaSession.ConnectionResult {
        val connectionResult = super.onConnect(session, controller)

        val customPlayerCommands = connectionResult.availablePlayerCommands.buildUpon()
            .add(Player.COMMAND_SEEK_TO_NEXT)
            .add(Player.COMMAND_SEEK_TO_PREVIOUS)
            .add(Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM)
            .add(Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM)
            .build()

        val availableSessionCommands = connectionResult.availableSessionCommands.buildUpon()
            .add(PLAY_STATION_COMMAND)
            .build()

        return MediaSession.ConnectionResult.accept(
            availableSessionCommands,
            customPlayerCommands
        )
    }

    override fun onSetMediaItems(
        mediaSession: MediaSession,
        controller: MediaSession.ControllerInfo,
        mediaItems: List<MediaItem>,
        startIndex: Int,
        startPositionMs: Long
    ): com.google.common.util.concurrent.ListenableFuture<MediaSession.MediaItemsWithStartPosition> {
        Timber.tag(TAG).d(
            "onSetMediaItems controller=%s size=%d startIndex=%d startPositionMs=%d",
            controller.packageName,
            mediaItems.size,
            startIndex,
            startPositionMs
        )
        return Futures.immediateFuture(
            MediaSession.MediaItemsWithStartPosition(
                mediaItems.map(::resolveMediaItem),
                startIndex,
                startPositionMs
            )
        )
    }

    override fun onAddMediaItems(
        mediaSession: MediaSession,
        controller: MediaSession.ControllerInfo,
        mediaItems: List<MediaItem>
    ): com.google.common.util.concurrent.ListenableFuture<List<MediaItem>> {
        Timber.tag(TAG).d(
            "onAddMediaItems controller=%s size=%d",
            controller.packageName,
            mediaItems.size
        )
        return Futures.immediateFuture(mediaItems.map(::resolveMediaItem))
    }

    override fun onCustomCommand(
        session: MediaSession,
        controller: MediaSession.ControllerInfo,
        customCommand: SessionCommand,
        args: Bundle
    ): com.google.common.util.concurrent.ListenableFuture<SessionResult> {
        Timber.tag(TAG).d(
            "onCustomCommand controller=%s action=%s",
            controller.packageName,
            customCommand.customAction
        )

        return when (customCommand.customAction) {
            ACTION_PLAY_STATION -> handlePlayStationCommand(session, args)
            else -> Futures.immediateFuture(SessionResult(SessionResult.RESULT_ERROR_NOT_SUPPORTED))
        }
    }

    override fun onPlaybackResumption(
        mediaSession: MediaSession,
        controller: MediaSession.ControllerInfo,
        isForPlayback: Boolean
    ): com.google.common.util.concurrent.ListenableFuture<MediaSession.MediaItemsWithStartPosition> {
        Timber.tag(TAG).d(
            "onPlaybackResumption controller=%s isForPlayback=%s",
            controller.packageName,
            isForPlayback
        )

        val currentMediaItem = mediaSession.player.currentMediaItem
        if (currentMediaItem != null) {
            val startPositionMs = if (isForPlayback) mediaSession.player.currentPosition else C.TIME_UNSET
            return Futures.immediateFuture(
                MediaSession.MediaItemsWithStartPosition(
                    listOf(currentMediaItem),
                    C.INDEX_UNSET,
                    startPositionMs
                )
            )
        }

        val fallbackItem = runBlocking {
            radioLibraryCatalog.loadChildren(page = 0, pageSize = 1).firstOrNull()
        } ?: radioLibraryCatalog.rootItem()

        val startPositionMs = if (isForPlayback) 0L else C.TIME_UNSET
        return Futures.immediateFuture(
            MediaSession.MediaItemsWithStartPosition(
                listOf(fallbackItem),
                0,
                startPositionMs
            )
        )
    }

    override fun onMediaButtonEvent(
        session: MediaSession,
        controllerInfo: MediaSession.ControllerInfo,
        intent: Intent
    ): Boolean {
        Timber.tag(TAG).d(
            "onMediaButtonEvent controller=%s action=%s",
            controllerInfo.packageName,
            intent.action
        )
        return false
    }

    override fun onPlayerInteractionFinished(
        session: MediaSession,
        controllerInfo: MediaSession.ControllerInfo,
        playerCommands: Player.Commands
    ) {
        Timber.tag(TAG).d(
            "onPlayerInteractionFinished controller=%s commands=%s",
            controllerInfo.packageName,
            playerCommands
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
            radioLibraryCatalog.loadChildren(page, pageSize)
        }

        return Futures.immediateFuture(LibraryResult.ofItemList(stations, params))
    }

    private fun resolveMediaItem(requested: MediaItem): MediaItem {
        return runBlocking {
            radioLibraryCatalog.findChild(requested.mediaId)
        } ?: requested
    }

    private fun handlePlayStationCommand(
        session: MediaSession,
        args: Bundle
    ): com.google.common.util.concurrent.ListenableFuture<SessionResult> {
        val mediaId = args.getString(ARG_MEDIA_ID).orEmpty()
        if (mediaId.isBlank()) {
            return Futures.immediateFuture(SessionResult(SessionResult.RESULT_ERROR_BAD_VALUE))
        }

        val mediaItem = runBlocking {
            radioLibraryCatalog.findChild(mediaId)
        } ?: return Futures.immediateFuture(SessionResult(SessionResult.RESULT_ERROR_BAD_VALUE))

        val startPositionMs = args.getLong(ARG_START_POSITION_MS, 0L)
        Timber.tag(TAG).d(
            "play station mediaId=%s startPositionMs=%d",
            mediaId,
            startPositionMs
        )

        session.player.setMediaItem(mediaItem, startPositionMs)
        session.player.prepare()
        session.player.play()
        return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
    }

    private companion object {
        const val TAG = "RadioSessionCallback"
        const val ACTION_PLAY_STATION = "io.github.agimaulana.radio.action.PLAY_STATION"
        const val ARG_MEDIA_ID = "media_id"
        const val ARG_START_POSITION_MS = "start_position_ms"

        val PLAY_STATION_COMMAND = SessionCommand(ACTION_PLAY_STATION, Bundle.EMPTY)
    }
}
