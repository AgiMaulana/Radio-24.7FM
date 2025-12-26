package io.github.agimaulana.radio.feature.stationlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.agimaulana.radio.core.radioplayer.PlaybackEvent
import io.github.agimaulana.radio.core.radioplayer.PlaybackState
import io.github.agimaulana.radio.core.radioplayer.RadioMediaItem
import io.github.agimaulana.radio.core.radioplayer.RadioPlayerController
import io.github.agimaulana.radio.core.radioplayer.RadioPlayerControllerFactory
import io.github.agimaulana.radio.core.radioplayer.extension.playImmediately
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
    private val radioPlayerControllerFactory: RadioPlayerControllerFactory,
) : ViewModel() {
    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    private var radioPlayerController: RadioPlayerController? = null

    fun init() {
        viewModelScope.launch {
            fetchRadioStations()
        }
        viewModelScope.launch {
            radioPlayerController = radioPlayerControllerFactory.get()
            viewModelScope.launch {
                radioPlayerController?.event?.collect(::onPlaybackEventReceived)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        radioPlayerController?.release()
    }

    fun onAction(action: Action) {
        when (action) {
            Action.LoadMore -> viewModelScope.launch {
                fetchRadioStations()
            }

            is Action.Click -> {
                if (radioPlayerController?.currentMediaId == action.station.serverUuid) {
                    if (radioPlayerController?.isPlaying == true) {
                        radioPlayerController?.pause()
                    } else {
                        radioPlayerController?.play()
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            selectedStation = action.station,
                            stations = it.stations.togglePlayingStateForStations(
                                isPlaying = false,
                                targetUuid = action.station.serverUuid
                            )
                        )
                    }
                    radioPlayerController?.playImmediately(action.station.toRadioMediaItem())
                }
            }

            is Action.Pause -> {
                radioPlayerController?.pause()
            }

            is Action.Play -> {
                radioPlayerController?.play()
            }

            is Action.Stop -> {
                radioPlayerController?.stop()
                _uiState.update {
                    it.copy(selectedStation = null)
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

    private fun onPlaybackEventReceived(playbackEvent: PlaybackEvent) {
        when (playbackEvent) {
            is PlaybackEvent.PlayingChanged -> {
                val station = _uiState.value.selectedStation?.copy(
                    isPlaying = playbackEvent.isPlaying,
                    isBuffering = false,
                )
                _uiState.update {
                    it.copy(
                        selectedStation = station,
                        stations = it.stations.togglePlayingStateForStations(
                            isPlaying = playbackEvent.isPlaying,
                            targetUuid = station?.serverUuid.orEmpty()
                        )
                    )
                }
            }

            is PlaybackEvent.StateChanged -> {
                val station = _uiState.value.selectedStation?.copy(
                    isBuffering = playbackEvent.state == PlaybackState.BUFFERING,
                )
                _uiState.update {
                    it.copy(selectedStation = station)
                }
            }
        }
    }

    private fun RadioStation.toUiStateStation(): UiState.Station {
        return UiState.Station(
            serverUuid = stationUuid,
            name = name,
            genre = tags.getOrNull(0).orEmpty(),
            imageUrl = imageUrl,
            streamUrl = url,
            isBuffering = false,
            isPlaying = false,
        )
    }

    private fun UiState.Station.toRadioMediaItem(): RadioMediaItem {
        return RadioMediaItem(
            mediaId = serverUuid,
            streamUrl = streamUrl,
            radioMetadata = RadioMediaItem.RadioMetadata(
                stationName = name,
                genre = genre,
                imageUrl = imageUrl,
            )
        )
    }

    private fun UiState.Station.togglePlayingState(isPlaying: Boolean, targetUuid: String): UiState.Station? {
        return if (serverUuid == targetUuid) copy(isPlaying = !isPlaying) else copy(isPlaying = false)
    }

    private fun ImmutableList<UiState.Station>.togglePlayingStateForStations(
        isPlaying: Boolean,
        targetUuid: String
    ): ImmutableList<UiState.Station> {
        return map { s ->
            if (s.serverUuid == targetUuid) s.copy(isPlaying = !isPlaying)
            else s.copy(isPlaying = false)
        }.toPersistentList()
    }

    data class UiState(
        val currentPage: Int = 0,
        val stations: ImmutableList<Station> = persistentListOf(),
        val selectedStation: Station? = null,
    ) {
        data class Station(
            val serverUuid: String,
            val name: String,
            val genre: String,
            val imageUrl: String,
            val streamUrl: String,
            val isBuffering: Boolean,
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
