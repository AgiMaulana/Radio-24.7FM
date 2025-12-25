package io.github.agimaulana.radio.core.radioplayer

import kotlinx.coroutines.flow.Flow

interface RadioPlayerController {
    val event: Flow<PlaybackEvent>
    val currentMediaId: String?
    fun setMediaItem(radioMediaItem: RadioMediaItem)
    fun prepare()
    fun play()
    fun pause()
    fun stop()
    fun release()
}
