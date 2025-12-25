package io.github.agimaulana.radio.core.radioplayer

import androidx.media3.common.Player

sealed interface PlaybackEvent {
    data class StateChanged(val state: PlaybackState) : PlaybackEvent
    data class PlayingChanged(val isPlaying: Boolean) : PlaybackEvent
}
