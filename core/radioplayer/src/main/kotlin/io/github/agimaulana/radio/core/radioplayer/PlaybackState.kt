package io.github.agimaulana.radio.core.radioplayer

import androidx.media3.common.Player

enum class PlaybackState {
    PLAYING,
    STOPPED,
    BUFFERING,
    IDLE;

    companion object {
        internal fun fromExoPlayerState(state: Int): PlaybackState {
            return when (state) {
                Player.STATE_BUFFERING -> BUFFERING
                Player.STATE_ENDED -> STOPPED
                Player.STATE_IDLE -> IDLE
                Player.STATE_READY -> PLAYING
                else -> throw IllegalArgumentException("Unknown state: $state")
            }
        }
    }
}
