package io.github.agimaulana.radio.core.radioplayer.internal

import androidx.media3.session.MediaController
import io.github.agimaulana.radio.core.radioplayer.PlaybackEvent
import io.github.agimaulana.radio.core.radioplayer.PlaybackState
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

    override fun setMediaItem(radioMediaItem: RadioMediaItem) {
        mediaController.setMediaItem(radioMediaItem.toMediaItem())
    }

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