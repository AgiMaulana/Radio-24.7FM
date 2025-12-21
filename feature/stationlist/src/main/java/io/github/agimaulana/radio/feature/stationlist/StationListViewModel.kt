package io.github.agimaulana.radio.feature.stationlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
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

@HiltViewModel
class StationListViewModel @Inject constructor(
    private val getRadioStationsUseCase: GetRadioStationsUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    fun init() {
        viewModelScope.launch {
            fetchRadioStations()
        }
    }

    fun onAction(action: Action) {
        when (action) {
            Action.LoadMore -> viewModelScope.launch {
                fetchRadioStations()
            }

            is Action.Click -> {
                if (_uiState.value.playing?.serverUuid == action.station.serverUuid) {
                    _uiState.update {
                        val isPlaying = _uiState.value.playing?.isPlaying == true
                        it.copy(
                            playing = it.playing?.copy(isPlaying = !isPlaying),
                            stations = it.stations.map { s ->
                                if (s.serverUuid == action.station.serverUuid) {
                                    s.copy(isPlaying = !isPlaying)
                                } else {
                                    s.copy(isPlaying = false)
                                }
                            }.toPersistentList(),
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            playing = action.station.copy(isPlaying = true),
                            stations = it.stations.map { s ->
                                if (s.serverUuid == action.station.serverUuid) {
                                    s.copy(isPlaying = true)
                                } else {
                                    s.copy(isPlaying = false)
                                }
                            }.toPersistentList(),
                        )
                    }
                }
            }

            is Action.Play -> {
                _uiState.update {
                    it.copy(
                        playing = action.station.copy(isPlaying = true),
                        stations = it.stations.map { s ->
                            if (s.serverUuid == action.station.serverUuid) {
                                s.copy(isPlaying = true)
                            } else {
                                s.copy(isPlaying = false)
                            }
                        }.toPersistentList(),
                    )
                }
            }

            is Action.Pause -> {
                _uiState.update {
                    it.copy(
                        playing = action.station.copy(isPlaying = false),
                        stations = it.stations.map { s ->
                            s.copy(isPlaying = false)
                        }.toPersistentList()
                    )
                }
            }

            is Action.Stop -> {
                _uiState.update {
                    it.copy(
                        playing = null,
                        stations = it.stations.map { s ->
                            s.copy(isPlaying = false)
                        }.toPersistentList()
                    )
                }
            }
        }
    }

    private suspend fun fetchRadioStations() {
        val stations = getRadioStationsUseCase.execute(page = _uiState.value.currentPage + 1)
            .map { it.toUiStateStation() }
            .toPersistentList()
        _uiState.update {
            it.copy(
                currentPage = it.currentPage + 1,
                stations = (it.stations + stations).toPersistentList()
            )
        }
    }

    private fun RadioStation.toUiStateStation(): UiState.Station {
        return UiState.Station(
            serverUuid = stationUuid,
            name = name,
            genre = tags.getOrNull(0).orEmpty(),
            imageUrl = imageUrl,
            isPlaying = false
        )
    }

    data class UiState(
        val currentPage: Int = 0,
        val stations: ImmutableList<Station> = persistentListOf(),
        val playing: Station? = null,
    ) {
        data class Station(
            val serverUuid: String,
            val name: String,
            val genre: String,
            val imageUrl: String,
            val isPlaying: Boolean,
        )
    }

    sealed interface Action {
        data object LoadMore : Action
        data class Click(val station: UiState.Station) : Action
        data class Play(val station: UiState.Station) : Action
        data class Pause(val station: UiState.Station) : Action
        data class Stop(val station: UiState.Station) : Action
    }
}
