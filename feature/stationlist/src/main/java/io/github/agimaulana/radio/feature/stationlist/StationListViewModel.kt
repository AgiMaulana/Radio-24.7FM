package io.github.agimaulana.radio.feature.stationlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.agimaulana.radio.core.radioplayer.PlaybackEvent
import io.github.agimaulana.radio.core.radioplayer.PlaybackState
import io.github.agimaulana.radio.core.radioplayer.RadioMediaItem
import io.github.agimaulana.radio.core.radioplayer.RadioPlayerController
import io.github.agimaulana.radio.core.radioplayer.RadioPlayerControllerFactory
import io.github.agimaulana.radio.domain.api.entity.RadioStation
import io.github.agimaulana.radio.domain.api.usecase.GetRadioStationUseCase
import io.github.agimaulana.radio.domain.api.usecase.GetRadioStationsUseCase
import android.content.Context
import androidx.compose.ui.graphics.Color
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.agimaulana.radio.feature.stationlist.player.PlayerColors
import io.github.agimaulana.radio.feature.stationlist.player.extractPlayerColors
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class StationListViewModel @Inject constructor(
    private val getRadioStationsUseCase: GetRadioStationsUseCase,
    private val getRadioStationUseCase: GetRadioStationUseCase,
    private val radioPlayerControllerFactory: RadioPlayerControllerFactory,
    private val stationListTracker: StationListTracker,
    @ApplicationContext private val context: Context,
) : ViewModel() {
    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    private var radioPlayerController: RadioPlayerController? = null
    private var searchJob: Job? = null

    fun init() {
        stationListTracker.trackScreenViewed()
        viewModelScope.launch {
            fetchRadioStations()
        }
        viewModelScope.launch {
            radioPlayerController = radioPlayerControllerFactory.get()
            viewModelScope.launch {
                radioPlayerController?.event?.collect(::onPlaybackEventReceived)
            }
            viewModelScope.launch {
                restoreSelectedStation()
            }
        }
    }

    private suspend fun restoreSelectedStation() {
        val mediaId = radioPlayerController?.currentMediaId ?: return
        if (mediaId.isNotEmpty()) {
            val station = getRadioStationUseCase.execute(mediaId)
            station.let {
                val uiStation = it.toUiStateStation().copy(
                    isPlaying = radioPlayerController?.isPlaying == true
                )
                _uiState.update { state ->
                    state.copy(selectedStation = uiStation)
                }
                updatePlayerColors(uiStation.imageUrl)
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
                stationListTracker.trackLoadMore(page = _uiState.value.currentPage + 1)
                fetchRadioStations()
            }

            is Action.Search -> {
                searchJob?.cancel()
                _uiState.update {
                    it.copy(
                        filterStationName = action.stationName,
                        currentPage = 0,
                        stations = persistentListOf(),
                        hasMorePages = true,
                    )
                }
                searchJob = viewModelScope.launch {
                    delay(SEARCH_DEBOUNCE_MS)
                    if (!action.stationName.isNullOrBlank()) {
                        stationListTracker.trackSearchSubmitted(action.stationName)
                    }
                    fetchRadioStations()
                }
            }

            is Action.Click -> {
                if (radioPlayerController?.currentMediaId == action.station.serverUuid) {
                    if (radioPlayerController?.isPlaying == true) {
                        stationListTracker.trackPlaybackPaused(action.station.serverUuid)
                        radioPlayerController?.pause()
                    } else {
                        stationListTracker.trackPlaybackResumed(action.station.serverUuid)
                        radioPlayerController?.play()
                    }
                } else {
                    stationListTracker.trackStationSelected(
                        stationId = action.station.serverUuid,
                        stationName = action.station.name,
                    )
                    radioPlayerController?.playImmediately(action.station.toRadioMediaItem())
                    updatePlayerColors(action.station.imageUrl)
                }
            }

            is Action.Pause -> {
                stationListTracker.trackPlaybackPaused(_uiState.value.selectedStation?.serverUuid)
                radioPlayerController?.pause()
            }

            is Action.Play -> {
                stationListTracker.trackPlaybackResumed(_uiState.value.selectedStation?.serverUuid)
                radioPlayerController?.play()
            }

            is Action.Stop -> {
                stationListTracker.trackPlaybackStopped(_uiState.value.selectedStation?.serverUuid)
                radioPlayerController?.stop()
                _uiState.update {
                    it.copy(selectedStation = null)
                }
            }

            is Action.ExpandPlayer -> {
                val selectedStation = _uiState.value.selectedStation
                stationListTracker.trackPlayerExpanded(
                    source = action.source,
                    stationId = selectedStation?.serverUuid,
                    stationName = selectedStation?.name,
                    isPlaying = selectedStation?.isPlaying == true,
                )
            }

            is Action.CollapsePlayer -> {
                val selectedStation = _uiState.value.selectedStation
                stationListTracker.trackPlayerCollapsed(
                    source = action.source,
                    stationId = selectedStation?.serverUuid,
                    stationName = selectedStation?.name,
                    isPlaying = selectedStation?.isPlaying == true,
                )
            }
        }
    }

    private suspend fun fetchRadioStations() {
        if (_uiState.value.isLoading || !_uiState.value.hasMorePages) return

        _uiState.update { it.copy(isLoading = true) }

        try {
            val nextPage = _uiState.value.currentPage + 1
            val fetchedStations = getRadioStationsUseCase.execute(
                page = nextPage,
                searchName = _uiState.value.filterStationName
            )
                .map {
                    val isCurrentlyPlaying = radioPlayerController?.currentMediaId == it.stationUuid
                            && radioPlayerController?.isPlaying == true
                    it.toUiStateStation().copy(isPlaying = isCurrentlyPlaying)
                }
                .toPersistentList()
            _uiState.update {
                it.copy(
                    currentPage = nextPage,
                    stations = (it.stations + fetchedStations).toPersistentList(),
                    hasMorePages = fetchedStations.isNotEmpty() && fetchedStations.size >= PAGE_SIZE,
                    isLoading = false
                )
            }
        } catch (e: Exception) {
            Timber.tag("StationListViewModel").e(e, "Error fetching radio stations")
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private fun onPlaybackEventReceived(playbackEvent: PlaybackEvent) {
        when (playbackEvent) {
            is PlaybackEvent.PlayingChanged -> {
                val station = _uiState.value.selectedStation
                _uiState.update {
                    it.copy(
                        selectedStation = station?.copy(
                            isPlaying = playbackEvent.isPlaying,
                            isBuffering = false,
                        ),
                        stations = it.stations.togglePlayingStateForStations(
                            targetUuid = radioPlayerController?.currentMediaId.orEmpty(),
                            isPlaying = playbackEvent.isPlaying
                        )
                    )
                }
            }

            is PlaybackEvent.StateChanged -> {
                val station = _uiState.value.selectedStation
                _uiState.update {
                    it.copy(
                        selectedStation = station?.copy(
                            isBuffering = playbackEvent.state == PlaybackState.BUFFERING,
                        )
                    )
                }
            }

            is PlaybackEvent.MediaItemTransition -> {
                val mediaId = playbackEvent.mediaId ?: return
                viewModelScope.launch {
                    val station = getRadioStationUseCase.execute(mediaId)
                    val uiStation = station.toUiStateStation().copy(
                        isPlaying = radioPlayerController?.isPlaying == true,
                        isBuffering = _uiState.value.selectedStation?.isBuffering ?: false
                    )
                    _uiState.update {
                        it.copy(selectedStation = uiStation)
                    }
                    updatePlayerColors(uiStation.imageUrl)
                }
            }
        }
    }

    private fun updatePlayerColors(imageUrl: String?) {
        viewModelScope.launch {
            val colors = extractPlayerColors(imageUrl, context)
            _uiState.update { it.copy(playerColors = colors) }
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

    private fun ImmutableList<UiState.Station>.togglePlayingStateForStations(
        isPlaying: Boolean,
        targetUuid: String
    ): ImmutableList<UiState.Station> {
        return map { s ->
            if (s.serverUuid == targetUuid) s.copy(isPlaying = isPlaying)
            else s.copy(isPlaying = false)
        }.toPersistentList()
    }

    private fun RadioPlayerController.playImmediately(radioMediaItem: RadioMediaItem) {
        setMediaItem(radioMediaItem)
        prepare()
        play()
    }

    data class UiState(
        val filterStationName: String? = null,
        val currentPage: Int = 0,
        val stations: ImmutableList<Station> = persistentListOf(),
        val selectedStation: Station? = null,
        val hasMorePages: Boolean = true,
        val isLoading: Boolean = false,
        val playerColors: PlayerColors = PlayerColors(
            dominant = Color(0xFF1C1A24),
            vibrant = Color(0xFF3a1040),
            darkMuted = Color(0xFF0e0c14),
        ),
        val featureFlag: FeatureFlag = FeatureFlag(),
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

        data class FeatureFlag(
            val isMoreMenuEnabled: Boolean = false,
            val isFavoriteEnabled: Boolean = false,
            val isActionRowEnabled: Boolean = false,
        )
    }

    sealed interface Action {
        data object LoadMore : Action
        data class Search(val stationName: String): Action
        data class Click(val station: UiState.Station) : Action
        data class Play(val station: UiState.Station) : Action
        data class Pause(val station: UiState.Station) : Action
        data class Stop(val station: UiState.Station) : Action
        data class ExpandPlayer(val source: String) : Action
        data class CollapsePlayer(val source: String) : Action
    }

    companion object {
        internal const val SEARCH_DEBOUNCE_MS = 300L
        internal const val PAGE_SIZE = 10
    }
}
