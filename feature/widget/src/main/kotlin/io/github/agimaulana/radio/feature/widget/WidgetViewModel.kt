package io.github.agimaulana.radio.feature.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.agimaulana.radio.core.radioplayer.PlaybackEvent
import io.github.agimaulana.radio.core.radioplayer.PlaybackState
import io.github.agimaulana.radio.core.radioplayer.RadioBrowserController
import io.github.agimaulana.radio.core.radioplayer.RadioBrowserFactory
import io.github.agimaulana.radio.core.radioplayer.RadioPlayerController
import io.github.agimaulana.radio.core.radioplayer.RadioPlayerControllerFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class WidgetViewModel @Inject constructor(
    private val radioBrowserFactory: RadioBrowserFactory,
    private val radioPlayerControllerFactory: RadioPlayerControllerFactory,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    private val bitmaps = MutableStateFlow<Map<String, Bitmap>>(emptyMap())

    private var radioBrowser: RadioBrowserController? = null
    private var radioPlayer: RadioPlayerController? = null
    private var observationJob: Job? = null

    fun init() {
        if (radioBrowser != null) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                radioBrowser = radioBrowserFactory.get()
                radioPlayer = radioPlayerControllerFactory.get()
                startObservation()
            } catch (t: Throwable) {
                Timber.e(t, "Failed to initialize WidgetViewModel")
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun startObservation() {
        val browser = radioBrowser ?: return
        val player = radioPlayer ?: return

        observationJob?.cancel()
        observationJob = viewModelScope.launch {
            var playbackState = PlaybackState.IDLE

            combine(
                browser.pinnedStations,
                merge(
                    flowOf(PlaybackEvent.StateChanged(playbackState)),
                    player.event
                ),
                bitmaps
            ) { pinned, event, currentBitmaps ->
                if (event is PlaybackEvent.StateChanged) {
                    playbackState = event.state
                }

                val currentMediaId = player.currentMediaId
                val isPlaying = player.isPlaying || playbackState == PlaybackState.BUFFERING

                pinned.mapNotNull { item ->
                    try {
                        val station = browser.getStation(item.mediaId)
                        station?.let {
                            val name = it.radioMetadata.stationName
                            val imageUrl = it.radioMetadata.imageUrl
                            loadIcon(item.mediaId, imageUrl)
                            PinnedTile(
                                mediaId = item.mediaId,
                                name = name,
                                frequency = "",
                                shortName = name.take(3).uppercase(),
                                brandColor = android.graphics.Color.DKGRAY,
                                isPlaying = isPlaying && item.mediaId == currentMediaId,
                                imageUrl = imageUrl,
                                imageBitmap = currentBitmaps[item.mediaId]
                            )
                        }
                    } catch (t: Throwable) {
                        Timber.e(t, "Failed to fetch station details for mediaId: ${item.mediaId}")
                        null
                    }
                }
            }.collectLatest { details ->
                _uiState.update { it.copy(isLoading = false, pinnedStationDetails = details) }
            }
        }
    }

    private fun loadIcon(mediaId: String, url: String?) {
        if (url.isNullOrBlank() || bitmaps.value.containsKey(mediaId)) return
        viewModelScope.launch {
            try {
                val loader = context.imageLoader
                val request = ImageRequest.Builder(context)
                    .data(url)
                    .size(120, 120)
                    .allowHardware(false)
                    .build()
                val result = loader.execute(request)
                if (result is SuccessResult) {
                    val bitmap = (result.drawable as? BitmapDrawable)?.bitmap
                    if (bitmap != null) {
                        bitmaps.update { it + (mediaId to bitmap) }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to load icon for $mediaId")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        observationJob?.cancel()
        radioBrowser?.release()
        radioPlayer?.release()
    }

    data class UiState(
        val isLoading: Boolean = false,
        val pinnedStationDetails: List<PinnedTile> = emptyList(),
    )
}
