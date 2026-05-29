package io.github.agimaulana.radio.feature.widget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.agimaulana.radio.core.radioplayer.RadioBrowserController
import io.github.agimaulana.radio.core.radioplayer.RadioBrowserFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class WidgetViewModel @Inject constructor(
    private val radioBrowserFactory: RadioBrowserFactory,
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    private var radioBrowser: RadioBrowserController? = null
    private var pinnedStationsJob: Job? = null

    fun init() {
        // Start a browser and observe pinned stations. Safe to call multiple times.
        if (radioBrowser != null) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val browser = try {
                radioBrowserFactory.get()
            } catch (t: Throwable) {
                Timber.e(t, "Failed to start RadioBrowserController for widget")
                _uiState.update { it.copy(isLoading = false) }
                return@launch
            }
            radioBrowser = browser
            observePinnedStations(browser)
        }
    }

    private fun observePinnedStations(browser: RadioBrowserController) {
        pinnedStationsJob?.cancel()
        pinnedStationsJob = viewModelScope.launch {
            browser.pinnedStations.collect { list ->
                val ids = list.map { it.mediaId }
                // Also attempt to fetch station details for up to 4 pinned stations
                val details = ids.take(4).mapNotNull { mediaId ->
                    try {
                        val station = browser.getStation(mediaId)
                        station?.let {
                            val name = it.radioMetadata.stationName
                            io.github.agimaulana.radio.feature.widget.PinnedTile(
                                mediaId = mediaId,
                                name = name,
                                frequency = "",
                                shortName = name.take(3).uppercase(),
                                brandColor = android.graphics.Color.DKGRAY,
                            )
                        }
                    } catch (t: Throwable) {
                        null
                    }
                }
                _uiState.update { it.copy(isLoading = false, pinnedStations = ids, pinnedStationDetails = details) }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        pinnedStationsJob?.cancel()
        radioBrowser?.release()
    }

    data class UiState(
        val isLoading: Boolean = false,
        val pinnedStations: List<String> = emptyList(),
        // detailed placeholder for widget rendering (not persisted)
        val pinnedStationDetails: List<io.github.agimaulana.radio.feature.widget.PinnedTile> = emptyList(),
    )
}
