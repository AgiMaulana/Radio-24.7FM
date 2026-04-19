package io.github.agimaulana.radio.core.radioplayer

sealed interface PlaybackEvent {
    data class StateChanged(val state: PlaybackState) : PlaybackEvent
    data class PlayingChanged(val isPlaying: Boolean) : PlaybackEvent
    data class MediaItemTransition(val mediaId: String?) : PlaybackEvent
}
