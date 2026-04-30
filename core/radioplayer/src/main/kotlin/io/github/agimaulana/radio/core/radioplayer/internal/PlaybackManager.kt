package io.github.agimaulana.radio.core.radioplayer.internal

import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import io.github.agimaulana.radio.core.radioplayer.RadioMediaItem
import io.github.agimaulana.radio.core.radioplayer.toMediaItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * PlaybackManager owns the playback queue and pagination triggers.
 * It is intentionally lightweight and accepts an optional fetcher lambda
 * to request next-page items when the player reaches the end.
 */
internal class PlaybackManager(
    private val player: Player,
    private val fetcher: suspend (
        nextPage: Int,
        query: String?
    ) -> List<RadioMediaItem> = { _: Int, _: String? -> emptyList() }
) {

    private val scope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())

    private val loadedItems = mutableListOf<RadioMediaItem>()

    private var isFetchingNextPage: Boolean = false

    private var _playbackContext: PlaybackContext = PlaybackContext(
        type = PlaybackContext.Type.DEFAULT,
        query = null
    )
    internal val playbackContext: PlaybackContext get() = _playbackContext

    private var _loadedIds: MutableSet<String> = mutableSetOf()
    internal val loadedIds: Set<String> get() = _loadedIds

    internal var nextPage: Int = 1
        private set

    private val playerListener = object : androidx.media3.common.Player.Listener {
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            maybeFetchNext()
        }
    }

    init {
        player.addListener(playerListener)
    }

    data class PlaybackContext(
        val type: Type,
        val query: String? = null,
    ) {
        enum class Type { DEFAULT, SEARCH, PINNED }
    }

    fun startPlayback(
        items: List<RadioMediaItem>,
        startIndex: Int = 0,
        context: PlaybackContext = PlaybackContext(PlaybackContext.Type.DEFAULT, null)
    ) {
        scope.launch {
            // Reset state
            loadedItems.clear()
            _loadedIds.clear()

            loadedItems.addAll(items)
            _loadedIds.addAll(items.map { it.mediaId })

            // Calculate starting page based on items size to avoid re-fetching initial pages
            nextPage = (items.size / PAGE_SIZE) + 1
            _playbackContext = context

            val mediaItems = items.map {
                it.toMediaItem(
                    contextType = context.type.name,
                    contextQuery = context.query,
                    page = null
                )
            }
            player.setMediaItems(mediaItems, startIndex, 0L)
            player.prepare()
            player.play()
        }
    }

    fun addMediaItems(items: List<RadioMediaItem>) {
        scope.launch {
            val filtered = items.filterNot { _loadedIds.contains(it.mediaId) }
            if (filtered.isEmpty()) return@launch

            loadedItems.addAll(filtered)
            _loadedIds.addAll(filtered.map { it.mediaId })

            val mediaItems = filtered.map {
                it.toMediaItem(
                    contextType = _playbackContext.type.name,
                    contextQuery = _playbackContext.query,
                    page = null
                )
            }
            player.addMediaItems(mediaItems)
        }
    }

    fun addMediaItems(index: Int, items: List<RadioMediaItem>) {
        scope.launch {
            val filtered = items.filterNot { _loadedIds.contains(it.mediaId) }
            if (filtered.isEmpty()) return@launch

            loadedItems.addAll(index, filtered)
            _loadedIds.addAll(filtered.map { it.mediaId })

            val mediaItems = filtered.map {
                it.toMediaItem(
                    contextType = _playbackContext.type.name,
                    contextQuery = _playbackContext.query,
                    page = null
                )
            }
            player.addMediaItems(index, mediaItems)
        }
    }

    private fun maybeFetchNext() {
        // Only attempt when at the last index, not already fetching, and not in PINNED context
        val isAtEnd = player.currentMediaItemIndex == player.mediaItemCount - 1
        if (!isAtEnd) return
        if (isFetchingNextPage) return
        if (_playbackContext.type == PlaybackContext.Type.PINNED) return

        scope.launch {
            fetchNextPage()
        }
    }

    suspend fun fetchNextPage() {
        if (isFetchingNextPage) return
        isFetchingNextPage = true
        try {
            val newItems = fetcher(nextPage, _playbackContext.query)
            if (newItems.isNotEmpty()) {
                val filtered = newItems.filterNot { _loadedIds.contains(it.mediaId) }
                if (filtered.isNotEmpty()) {
                    // append
                    loadedItems.addAll(filtered)
                    _loadedIds.addAll(filtered.map { it.mediaId })
                    val mediaItems = filtered.map {
                        it.toMediaItem(
                            contextType = _playbackContext.type.name,
                            contextQuery = _playbackContext.query,
                            page = nextPage
                        )
                    }
                    player.addMediaItems(mediaItems)
                }
                nextPage++
            }
        } finally {
            isFetchingNextPage = false
        }
    }

    fun release() {
        player.removeListener(playerListener)
        scope.cancel()
    }

    // Restore minimal playback context from player's current media item metadata.
    fun restoreFromPlayer() {
        val current = player.currentMediaItem ?: return
        val extras = current.mediaMetadata.extras
        val typeName = extras?.getString("playback_context_type") ?: PlaybackContext.Type.DEFAULT.name
        val query = extras?.getString("playback_context_query")
        _playbackContext = try {
            PlaybackContext(PlaybackContext.Type.valueOf(typeName), query)
        } catch (e: Exception) {
            Timber.i(e)
            PlaybackContext(PlaybackContext.Type.DEFAULT, null)
        }

        // Restore loadedIds from current player items to avoid duplicates during pagination
        _loadedIds.clear()
        repeat(player.mediaItemCount) { index ->
            player.getMediaItemAt(index).mediaId?.let { _loadedIds.add(it) }
        }

        // Estimate next page from current playlist size
        val itemCount = player.mediaItemCount
        nextPage = (itemCount / PAGE_SIZE) + 1
    }

    companion object {
        private const val PAGE_SIZE = 10
    }
}
