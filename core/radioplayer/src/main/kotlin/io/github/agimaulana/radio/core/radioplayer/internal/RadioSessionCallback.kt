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
import io.github.agimaulana.radio.core.radioplayer.PlaybackExtras
import io.github.agimaulana.radio.core.radioplayer.RadioLibraryContract
import io.github.agimaulana.radio.core.radioplayer.RadioPlayerController
import io.github.agimaulana.radio.domain.api.entity.GeoLatLong
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.guava.future
import timber.log.Timber

@OptIn(UnstableApi::class)
internal class RadioSessionCallback(
    private val radioLibraryCatalog: RadioLibraryCatalog,
) : MediaLibraryService.MediaLibrarySession.Callback {
    private val callbackScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

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
        Timber.tag(TAG).d(
            "onSetMediaItems controller=%s size=%d startIndex=%d startPositionMs=%d",
            controller.packageName,
            mediaItems.size,
            startIndex,
            startPositionMs
        )
        return callbackScope.future {
            val firstItem = mediaItems.firstOrNull()
            val context = firstItem?.mediaMetadata?.extras?.let { extractContext(it) }

            val (playlist, resolvedIndex) = if (firstItem != null && mediaItems.size == 1) {
                val catalogPlaylist = if (context != null) {
                    radioLibraryCatalog.getPlaylistForContext(context)
                } else {
                    radioLibraryCatalog.getPlaylist()
                }
                val index = catalogPlaylist.indexOfFirst { it.mediaId == firstItem.mediaId }
                if (index >= 0) {
                    catalogPlaylist to index
                } else {
                    val resolvedItem = radioLibraryCatalog.findChild(firstItem.mediaId) ?: firstItem
                    listOf(resolvedItem) to 0
                }
            } else {
                val hasNoUri = mediaItems.any { it.localConfiguration == null }
                val resolvedItems = if (hasNoUri) {
                    mediaItems.map { requested ->
                        radioLibraryCatalog.findChild(requested.mediaId) ?: requested
                    }
                } else {
                    mediaItems
                }
                resolvedItems to startIndex
            }

            MediaSession.MediaItemsWithStartPosition(
                playlist,
                resolvedIndex,
                startPositionMs
            )
        }
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
        return callbackScope.future {
            mediaItems.map { requested ->
                radioLibraryCatalog.findChild(requested.mediaId) ?: requested
            }
        }
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

        return Futures.immediateFuture(SessionResult(SessionResult.RESULT_ERROR_NOT_SUPPORTED))
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

        return callbackScope.future {
            val currentMediaItem = mediaSession.player.currentMediaItem
            val context = currentMediaItem?.mediaMetadata?.extras?.let { extractContext(it) }
            val playlist = if (context != null) {
                radioLibraryCatalog.getPlaylistForContext(context)
            } else {
                radioLibraryCatalog.getPlaylist()
            }
            val startPositionMs = if (isForPlayback) mediaSession.player.currentPosition else C.TIME_UNSET

            val (items, index) = if (currentMediaItem != null) {
                val foundIndex = playlist.indexOfFirst { it.mediaId == currentMediaItem.mediaId }
                if (foundIndex >= 0) {
                    playlist to foundIndex
                } else {
                    listOf(currentMediaItem) to C.INDEX_UNSET
                }
            } else {
                val fallbackItem = playlist.firstOrNull() ?: radioLibraryCatalog.rootItem()
                listOf(fallbackItem) to 0
            }

            MediaSession.MediaItemsWithStartPosition(items, index, startPositionMs)
        }
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

    override fun onGetItem(
        session: MediaLibraryService.MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        mediaId: String
    ): com.google.common.util.concurrent.ListenableFuture<LibraryResult<MediaItem>> {
        return callbackScope.future {
            radioLibraryCatalog.findChild(mediaId)?.let { item ->
                LibraryResult.ofItem(item, null)
            } ?: LibraryResult.ofError(LibraryResult.RESULT_ERROR_BAD_VALUE)
        }
    }

    override fun onGetChildren(
        session: MediaLibraryService.MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        parentId: String,
        page: Int,
        pageSize: Int,
        params: MediaLibraryService.LibraryParams?
    ): com.google.common.util.concurrent.ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
        return callbackScope.future {
            val items = when (parentId) {
                RadioLibraryContract.ROOT_MEDIA_ID -> listOf(
                    radioLibraryCatalog.pinnedItem(),
                    radioLibraryCatalog.stationsItem()
                )

                RadioLibraryContract.PINNED_MEDIA_ID -> radioLibraryCatalog.getPinned()

                RadioLibraryContract.STATIONS_MEDIA_ID -> {
                    radioLibraryCatalog.getStations(
                        page = page,
                        pageSize = pageSize,
                        search = params?.extras?.getString(RadioLibraryContract.EXTRA_SEARCH)
                            ?.takeIf { it.isNotBlank() },
                        location = params?.extras?.let { extras ->
                            if (
                                extras.containsKey(RadioLibraryContract.EXTRA_LOCATION_LAT) &&
                                extras.containsKey(RadioLibraryContract.EXTRA_LOCATION_LON)
                            ) {
                                GeoLatLong(
                                    latitude = extras.getDouble(RadioLibraryContract.EXTRA_LOCATION_LAT),
                                    longitude = extras.getDouble(RadioLibraryContract.EXTRA_LOCATION_LON)
                                )
                            } else {
                                null
                            }
                        }
                    )
                }

                else -> emptyList()
            }

            LibraryResult.ofItemList(items, params)
        }
    }

    fun close() {
        callbackScope.cancel()
    }

    private fun extractContext(extras: Bundle): RadioPlayerController.PlaybackContext? {
        val typeStr = extras.getString(PlaybackExtras.KEY_CONTEXT_TYPE) ?: return null
        val type = runCatching { RadioPlayerController.PlaybackContext.Type.valueOf(typeStr) }.getOrNull() ?: return null
        val query = extras.getString(PlaybackExtras.KEY_CONTEXT_QUERY)
        val lat = extras.getDouble(PlaybackExtras.KEY_CONTEXT_LAT, Double.NaN).takeIf { !it.isNaN() }
        val lon = extras.getDouble(PlaybackExtras.KEY_CONTEXT_LON, Double.NaN).takeIf { !it.isNaN() }
        val location = if (lat != null && lon != null) {
            RadioPlayerController.PlaybackContext.Location(lat, lon)
        } else {
            null
        }
        return RadioPlayerController.PlaybackContext(type, query, location)
    }

    private companion object {
        const val TAG = "RadioSessionCallback"
    }
}
