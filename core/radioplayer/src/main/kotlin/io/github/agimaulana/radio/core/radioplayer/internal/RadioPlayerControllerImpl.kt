package io.github.agimaulana.radio.core.radioplayer.internal

import androidx.media3.session.MediaController
import io.github.agimaulana.radio.core.radioplayer.PlaybackEvent
import io.github.agimaulana.radio.core.radioplayer.PlaybackState
import io.github.agimaulana.radio.core.radioplayer.RadioMediaItem
import io.github.agimaulana.radio.core.radioplayer.RadioPlayerController
import io.github.agimaulana.radio.core.radioplayer.toMediaItem
import io.github.agimaulana.radio.core.radioplayer.internal.ServiceResolver.resolvePlaybackStartCallback
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking

internal class RadioPlayerControllerImpl(
    private val mediaController: MediaController,
) : RadioPlayerController {

    private val playbackEventFlow = PlaybackEventFlow(mediaController)

    override val event: Flow<PlaybackEvent>
        get() = playbackEventFlow.event

    override val currentMediaId: String?
        get() = mediaController.currentMediaItem?.mediaId

    override val isPlaying: Boolean
        get() = mediaController.isPlaying

    // Single-item compatibility
    override fun setMediaItem(radioMediaItem: RadioMediaItem) {
        mediaController.setMediaItem(radioMediaItem.toMediaItem())
    }

    // Playlist APIs - Route startPlayback through PlaybackManager for proper state sync
    override fun startPlayback(items: List<RadioMediaItem>, startIndex: Int, contextType: String?, contextQuery: String?) {
        val callback = resolvePlaybackStartCallback()
        if (callback != null) {
            runBlocking {
                callback(items, startIndex, contextType, contextQuery)
            }
        } else {
            // Fallback: direct setMediaItems if PlaybackManager not available
            setMediaItems(items, startIndex, contextType, contextQuery)
            prepare()
            play()
        }
    }

    override fun setMediaItems(items: List<RadioMediaItem>, startIndex: Int, contextType: String?, contextQuery: String?) {
        val mediaItems = items.map { it.toMediaItem(contextType, contextQuery, null) }
        mediaController.setMediaItems(mediaItems, startIndex, 0L)
    }

    override fun addMediaItems(items: List<RadioMediaItem>, contextType: String?, contextQuery: String?) {
        val mediaItems = items.map { it.toMediaItem(contextType, contextQuery, null) }
        mediaController.addMediaItems(mediaItems)
    }

    override fun addMediaItems(index: Int, items: List<RadioMediaItem>, contextType: String?, contextQuery: String?) {
        val mediaItems = items.map { it.toMediaItem(contextType, contextQuery, null) }
        mediaController.addMediaItems(index, mediaItems)
    }

    override val mediaItemCount: Int
        get() = mediaController.mediaItemCount

    override val currentMediaItemIndex: Int
        get() = mediaController.currentMediaItemIndex

    override fun prepare() {
        mediaController.prepare()
    }

    override fun play() {
        mediaController.play()
    }

    override fun pause() {
        mediaController.pause()
    }

    override fun stop() {
        mediaController.stop()
    }

    override fun release() {
        playbackEventFlow.release()
        mediaController.release()
    }
}