package io.github.agimaulana.radio.core.radioplayer

import io.github.agimaulana.radio.core.radioplayer.RadioPlayerController.PlaybackContext.Type.DEFAULT
import kotlinx.coroutines.flow.Flow

interface RadioPlayerController {
    val event: Flow<PlaybackEvent>
    val currentMediaId: String?
    val isPlaying: Boolean

    // Single-item compatibility
    fun setMediaItem(radioMediaItem: RadioMediaItem)

    // Playlist APIs - Use startPlayback for proper PlaybackManager sync
    fun startPlayback(
        items: List<RadioMediaItem>,
        startIndex: Int = 0,
        context: PlaybackContext = PlaybackContext(DEFAULT, null)
    )

    fun setMediaItems(
        items: List<RadioMediaItem>,
        startIndex: Int = 0,
        context: PlaybackContext = PlaybackContext(DEFAULT, null)
    )

    fun addMediaItems(
        items: List<RadioMediaItem>,
        context: PlaybackContext = PlaybackContext(DEFAULT, null)
    )

    fun addMediaItems(
        index: Int,
        items: List<RadioMediaItem>,
        context: PlaybackContext = PlaybackContext(DEFAULT, null)
    )

    val mediaItemCount: Int
    val currentMediaItemIndex: Int

    fun prepare()
    fun play()
    fun pause()
    fun stop()
    fun release()

    data class PlaybackContext(
        val type: Type,
        val query: String? = null
    ) {
        enum class Type { DEFAULT, SEARCH, PINNED }
    }
}
