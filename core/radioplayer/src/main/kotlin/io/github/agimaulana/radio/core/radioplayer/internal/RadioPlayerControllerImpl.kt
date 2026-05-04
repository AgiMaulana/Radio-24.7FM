package io.github.agimaulana.radio.core.radioplayer.internal

import android.content.Context
import androidx.media3.session.MediaController
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.CastStateListener
import io.github.agimaulana.radio.core.radioplayer.PlaybackEvent
import io.github.agimaulana.radio.core.radioplayer.RadioMediaItem
import io.github.agimaulana.radio.core.radioplayer.RadioPlayerController
import io.github.agimaulana.radio.core.radioplayer.RadioPlayerController.CastState
import io.github.agimaulana.radio.core.radioplayer.toMediaItem
import io.github.agimaulana.radio.core.radioplayer.toRadioMediaItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class RadioPlayerControllerImpl(
    private val mediaController: MediaController,
    private val context: Context,
) : RadioPlayerController {

    private val playbackEventFlow = PlaybackEventFlow(mediaController)
    private val _castState = MutableStateFlow(CastState.NO_DEVICES)

    override val event: Flow<PlaybackEvent>
        get() = playbackEventFlow.event

    override val currentMediaId: String?
        get() = mediaController.currentMediaItem?.mediaId

    override val isPlaying: Boolean
        get() = mediaController.isPlaying

    override val castState: Flow<CastState> = _castState.asStateFlow()

    private val castStateListener = CastStateListener { state ->
        _castState.value = mapCastState(state)
    }

    init {
        try {
            CastContext.getSharedInstance(context)?.let { castContext ->
                castContext.addCastStateListener(castStateListener)
                _castState.value = mapCastState(castContext.castState)
            }
        } catch (e: Exception) {
            // Cast context might not be available
        }
    }

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

    override fun getPlaylist(): List<RadioMediaItem> {
        return (0 until mediaController.mediaItemCount).map {
            mediaController.getMediaItemAt(it).toRadioMediaItem()
        }
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
        try {
            CastContext.getSharedInstance(context)?.removeCastStateListener(castStateListener)
        } catch (e: Exception) {
            // Ignore
        }
        playbackEventFlow.release()
        mediaController.release()
    }

    private fun mapCastState(state: Int): CastState {
        return when (state) {
            com.google.android.gms.cast.framework.CastState.NO_DEVICES_AVAILABLE -> CastState.NO_DEVICES
            com.google.android.gms.cast.framework.CastState.NOT_CONNECTED -> CastState.NOT_CONNECTED
            com.google.android.gms.cast.framework.CastState.CONNECTING -> CastState.CONNECTING
            com.google.android.gms.cast.framework.CastState.CONNECTED -> CastState.CONNECTED
            else -> CastState.NO_DEVICES
        }
    }
}
