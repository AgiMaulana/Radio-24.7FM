package io.github.agimaulana.radio.core.radioplayer.internal

import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import io.github.agimaulana.radio.core.radioplayer.PlaybackEvent
import io.github.agimaulana.radio.core.radioplayer.PlaybackState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

class PlaybackEventFlow(
    private val mediaController: MediaController,
) : Player.Listener {
    private val _event = Channel<PlaybackEvent>()
    val event = _event.receiveAsFlow()

    init {
        mediaController.addListener(this)
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        _event.trySend(PlaybackEvent.PlayingChanged(isPlaying))
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        val state = PlaybackState.fromExoPlayerState(playbackState)
        _event.trySend(PlaybackEvent.StateChanged(state))
    }

    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        _event.trySend(PlaybackEvent.MediaItemTransition(mediaItem?.mediaId))
    }

    fun release() {
        mediaController.removeListener(this)
    }
}
