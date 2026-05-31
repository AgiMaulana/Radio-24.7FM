package io.github.agimaulana.radio.feature.widget

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.agimaulana.radio.core.radioplayer.RadioBrowserController
import io.github.agimaulana.radio.core.radioplayer.RadioBrowserFactory
import io.github.agimaulana.radio.core.radioplayer.RadioPlayerController
import io.github.agimaulana.radio.core.radioplayer.RadioPlayerControllerFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class WidgetViewModel @Inject constructor(
    private val radioBrowserFactory: RadioBrowserFactory,
    private val radioPlayerControllerFactory: RadioPlayerControllerFactory,
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

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
            combine(
                browser.pinnedStations,
                player.event
            ) { pinned, _ ->
                val currentMediaId = player.currentMediaId
                val isPlaying = player.isPlaying
                
                val details = pinned.mapNotNull { item ->
                    try {
                        val station = browser.getStation(item.mediaId)
                        station?.let {
                            val name = it.radioMetadata.stationName
                            PinnedTile(
                                mediaId = item.mediaId,
                                name = name,
                                frequency = "",
                                shortName = name.take(3).uppercase(),
                                brandColor = android.graphics.Color.DKGRAY,
                                isPlaying = isPlaying && item.mediaId == currentMediaId
                            )
                        }
                    } catch (t: Throwable) {
                        Timber.e(t, "Failed to fetch station details for mediaId: ${item.mediaId}")
                        null
                    }
                }
                details
            }.collectLatest { details ->
                _uiState.update { it.copy(isLoading = false, pinnedStationDetails = details) }
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
