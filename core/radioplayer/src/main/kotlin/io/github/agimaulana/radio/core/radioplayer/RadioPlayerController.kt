package io.github.agimaulana.radio.core.radioplayer

import io.github.agimaulana.radio.core.radioplayer.RadioPlayerController.PlaybackContext.Type.DEFAULT
import kotlinx.coroutines.flow.Flow

interface RadioPlayerController {
    val event: Flow<PlaybackEvent>
    val currentMediaId: String?
    val isPlaying: Boolean
    val castState: Flow<CastState>

    // Single-item compatibility
    fun setMediaItem(radioMediaItem: RadioMediaItem)

    // Playlist APIs - direct Media3 playlist operations
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

    fun getPlaylist(): List<RadioMediaItem>

    val mediaItemCount: Int
    val currentMediaItemIndex: Int

    fun prepare()
    fun play()
    fun pause()
    fun stop()
    fun release()

    enum class CastState {
        NO_DEVICES, NOT_CONNECTED, CONNECTING, CONNECTED
    }

    data class PlaybackContext(
        val type: Type,
        val query: String? = null
    ) {
        enum class Type { DEFAULT, SEARCH, PINNED }
    }
}
