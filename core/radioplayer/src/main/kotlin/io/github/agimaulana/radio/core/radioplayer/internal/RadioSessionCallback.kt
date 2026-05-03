package io.github.agimaulana.radio.core.radioplayer.internal

import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import kotlinx.coroutines.runBlocking

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

        return MediaSession.ConnectionResult.accept(
            connectionResult.availableSessionCommands,
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
        return Futures.immediateFuture(mediaItems.map(::resolveMediaItem))
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
}
