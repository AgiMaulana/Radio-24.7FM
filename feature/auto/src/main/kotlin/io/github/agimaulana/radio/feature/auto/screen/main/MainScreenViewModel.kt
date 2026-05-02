package io.github.agimaulana.radio.feature.auto.screen.main

import android.graphics.Bitmap
import android.util.Log
import androidx.car.app.CarContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil3.ImageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.toBitmap
import io.github.agimaulana.radio.domain.api.entity.RadioStation
import io.github.agimaulana.radio.domain.api.usecase.GetRadioStationsUseCase
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

class MainScreenViewModel @Inject constructor(
    private val getRadioStationsUseCase: GetRadioStationsUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    fun init() {
        viewModelScope.launch {
            val stations = getRadioStationsUseCase.execute(
                page = 1,
                searchName = null,
                location = null,
            ).toUiStateStations()
            _uiState.update {
                it.copy(
                    isLoading = false,
                    allStations = stations.toPersistentList()
                )
            }
        }
    }

    private val loadingStations = mutableSetOf<String>()

    fun onAction(action: Action) {
        when (action) {
            is Action.LoadImage -> {
                if (action.station.imageBitmap == null && !loadingStations.contains(action.station.serverUuid)) {
                    fetchImageBitmaps(action.carContext, action.station)
                }
            }
        }
    }

    private fun fetchImageBitmaps(carContext: CarContext, station: UiState.Station) {
        loadingStations.add(station.serverUuid)
        Log.d("ketai", "load image for station ${station.name}, url: ${station.imageUrl}")
        viewModelScope.launch {
            try {
                val bitmap = downloadBitmap(carContext, station.imageUrl)
                _uiState.update {
                    it.copy(
                        allStations = it.allStations.swap(
                            serverUuid = station.serverUuid,
                            station = station.copy(imageBitmap = bitmap)
                        )
                    )
                }
                Log.d("ketai", "image bitmap loaded and station swapped")
            } finally {
                loadingStations.remove(station.serverUuid)
            }
        }
    }

    private suspend fun downloadBitmap(carContext: CarContext, url: String): Bitmap? {
        val loader = ImageLoader(carContext)
        val request = ImageRequest.Builder(carContext)
            .data(url)
            .allowHardware(false)
            .build()

        return (loader.execute(request) as? SuccessResult)?.image?.toBitmap()
    }

    private fun List<UiState.Station>.swap(
        serverUuid: String,
        station: UiState.Station
    ): ImmutableList<UiState.Station> = map {
        if (it.serverUuid == serverUuid) {
            station
        } else {
            it
        }
    }.toPersistentList()

    private fun List<RadioStation>.toUiStateStations() = map { it.toUiStateStation() }

    private fun RadioStation.toUiStateStation() = UiState.Station(
        serverUuid = stationUuid,
        name = name,
        genre = tags.getOrNull(0).orEmpty(),
        imageUrl = imageUrl,
        imageBitmap = null,
        streamUrl = url,
        isBuffering = false,
        isPlaying = false,
        isPinned = false
    )

    data class UiState(
        val isLoading: Boolean = true,
        val allStations: ImmutableList<Station> = persistentListOf()
    ) {
        data class Station(
            val serverUuid: String,
            val name: String,
            val genre: String,
            val imageUrl: String,
            val imageBitmap: Bitmap?,
            val streamUrl: String,
            val isBuffering: Boolean,
            val isPlaying: Boolean,
            val isPinned: Boolean = false,
        )
    }

    sealed interface Action {
        data class LoadImage(val carContext: CarContext, val station: UiState.Station) : Action
    }
}
