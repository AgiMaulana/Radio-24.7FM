package io.github.agimaulana.radio.core.radioplayer.internal

import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber

internal class PlaylistPaginator(
    private var player: Player,
    private val catalog: RadioLibraryCatalog,
    private val scope: CoroutineScope,
) {
    private var isLoading = false
    private var lastLoadedPage = -1
    private var hasMorePages = true

    private val playerListener = object : Player.Listener {
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            checkPagination()
        }
    }

    init {
        player.addListener(playerListener)
    }

    fun updatePlayer(newPlayer: Player) {
        player.removeListener(playerListener)
        player = newPlayer
        player.addListener(playerListener)
        reset()
        checkPagination()
    }

    fun reset() {
        lastLoadedPage = -1
        hasMorePages = true
        isLoading = false
    }

    private fun checkPagination() {
        if (isLoading || !hasMorePages) return

        val currentIndex = player.currentMediaItemIndex
        val playlistSize = player.mediaItemCount

        if (currentIndex >= playlistSize - 2 && playlistSize > 0) {
            loadNextPage()
        }
    }

    private fun loadNextPage() {
        isLoading = true
        scope.launch {
            try {
                // Explicit page tracking: determine next page from catalog state
                val currentPage = catalog.getCurrentPage()
                val nextPage = if (lastLoadedPage == -1) {
                    // Initial pagination after fresh start or context switch
                    maxOf(currentPage, player.mediaItemCount / RadioLibraryCatalog.CATALOG_PAGE_SIZE)
                } else {
                    lastLoadedPage + 1
                }

                if (nextPage <= lastLoadedPage) {
                    Timber.d("Page %d already loaded, skipping", nextPage)
                    return@launch
                }

                Timber.d("Loading next page: %d", nextPage)
                val newItems = catalog.loadChildren(nextPage, RadioLibraryCatalog.CATALOG_PAGE_SIZE)
                
                if (newItems.isEmpty()) {
                    hasMorePages = false
                } else {
                    lastLoadedPage = nextPage
                    appendItems(newItems)
                }
            } catch (e: Exception) {
                Timber.e(e, "Error loading next page")
            } finally {
                isLoading = false
            }
        }
    }

    private fun appendItems(newItems: List<MediaItem>) {
        val currentIds = (0 until player.mediaItemCount).map { 
            player.getMediaItemAt(it).mediaId 
        }.toSet()
        
        val filtered = newItems.filter { it.mediaId !in currentIds }
        if (filtered.isNotEmpty()) {
            player.addMediaItems(filtered)
        }
    }

    fun release() {
        player.removeListener(playerListener)
    }
}
