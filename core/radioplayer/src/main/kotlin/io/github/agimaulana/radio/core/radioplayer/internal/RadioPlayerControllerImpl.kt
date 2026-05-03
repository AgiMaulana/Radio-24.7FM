package io.github.agimaulana.radio.core.radioplayer.internal

import androidx.media3.session.MediaController
import io.github.agimaulana.radio.core.radioplayer.PlaybackEvent
import io.github.agimaulana.radio.core.radioplayer.RadioMediaItem
import io.github.agimaulana.radio.core.radioplayer.RadioPlayerController
import io.github.agimaulana.radio.core.radioplayer.toMediaItem
import kotlinx.coroutines.flow.Flow

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

    // Playlist APIs - direct Media3 playlist operations
    override fun startPlayback(
        items: List<RadioMediaItem>,
        startIndex: Int,
        context: RadioPlayerController.PlaybackContext
    ) {
        setMediaItems(items, startIndex, context)
        prepare()
        play()
    }

    override fun setMediaItems(
        items: List<RadioMediaItem>,
        startIndex: Int,
        context: RadioPlayerController.PlaybackContext
    ) {
        val mediaItems = items.map { it.toMediaItem(context.type.name, context.query, null) }
        mediaController.setMediaItems(mediaItems, startIndex, 0L)
    }

    override fun addMediaItems(
        items: List<RadioMediaItem>,
        context: RadioPlayerController.PlaybackContext
    ) {
        val mediaItems = items.map { it.toMediaItem(context.type.name, context.query, null) }
        mediaController.addMediaItems(mediaItems)
    }

    override fun addMediaItems(
        index: Int,
        items: List<RadioMediaItem>,
        context: RadioPlayerController.PlaybackContext
    ) {
        val mediaItems = items.map {
            it.toMediaItem(
                contextType = context.type.name,
                contextQuery = context.query,
                page = null
            )
        }
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
