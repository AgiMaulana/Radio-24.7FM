package io.github.agimaulana.radio.feature.stationlist

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.agimaulana.radio.core.radioplayer.PlaybackEvent
import io.github.agimaulana.radio.core.radioplayer.PlaybackState
import io.github.agimaulana.radio.core.radioplayer.RadioMediaItem
import io.github.agimaulana.radio.core.radioplayer.RadioPlayerController
import io.github.agimaulana.radio.core.radioplayer.RadioPlayerControllerFactory
import io.github.agimaulana.radio.domain.api.entity.GeoLatLong
import io.github.agimaulana.radio.domain.api.entity.RadioStation
import io.github.agimaulana.radio.domain.api.usecase.GetRadioStationUseCase
import io.github.agimaulana.radio.domain.api.usecase.GetRadioStationsUseCase
import io.github.agimaulana.radio.feature.stationlist.location.LocationProvider
import io.github.agimaulana.radio.feature.stationlist.player.PlayerColors
import io.github.agimaulana.radio.feature.stationlist.player.extractPlayerColors
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class StationListViewModel @Inject constructor(
    private val getRadioStationsUseCase: GetRadioStationsUseCase,
    private val getRadioStationUseCase: GetRadioStationUseCase,
    private val radioPlayerControllerFactory: RadioPlayerControllerFactory,
    private val stationListTracker: StationListTracker,
    private val locationProvider: LocationProvider,
    @ApplicationContext private val context: Context,
) : ViewModel() {
    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    private var radioPlayerController: RadioPlayerController? = null
    private var searchJob: Job? = null
    private var fetchJob: Job? = null

fun init(
        hasLocationPermission: Boolean = false,
        hasAskedPermission: Boolean = false,
        shouldShowRationale: Boolean = false
    ) {
        stationListTracker.trackScreenViewed()
        val shouldShowSheet = !hasLocationPermission && (
            !hasAskedPermission || shouldShowRationale
        )
        _uiState.update { it.copy(showLocationPermissionSheet = shouldShowSheet) }
        viewModelScope.launch {
            radioPlayerController = radioPlayerControllerFactory.get().apply {
                viewModelScope.launch { event.collect(::onPlaybackEventReceived) }
            }
            restoreSelectedStation()
        }
    }

    private suspend fun restoreSelectedStation() {
        val mediaId = radioPlayerController?.currentMediaId ?: return
        if (mediaId.isNotEmpty()) {
            val station = getRadioStationUseCase.execute(mediaId)
            val uiStation = station.toUiStateStation().copy(isPlaying = radioPlayerController?.isPlaying == true)
            _uiState.update { it.copy(selectedStation = uiStation) }
            updatePlayerColors(uiStation.imageUrl)
        }
    }

    override fun onCleared() {
        super.onCleared()
        radioPlayerController?.release()
    }

    fun onAction(action: Action) {
        when (action) {
            Action.LoadMore -> {
                stationListTracker.trackLoadMore(page = _uiState.value.currentPage + 1)
                fetchRadioStations()
            }
            is Action.Search -> handleSearch(action.stationName)
            is Action.Click -> handleClick(action.station)
            is Action.Pause -> {
                stationListTracker.trackPlaybackPaused(_uiState.value.selectedStation?.serverUuid)
                radioPlayerController?.pause()
            }
            is Action.Play -> {
                stationListTracker.trackPlaybackResumed(_uiState.value.selectedStation?.serverUuid)
                radioPlayerController?.play()
            }
            is Action.Stop -> {
                stationListTracker.trackPlaybackStopped(action.station.serverUuid)
                radioPlayerController?.stop()
                _uiState.update { it.copy(selectedStation = null) }
            }
            is Action.ExpandPlayer -> trackPlayerEvent(action.source, true)
            is Action.CollapsePlayer -> trackPlayerEvent(action.source, false)
            is Action.OnLocationPermissionGranted -> handleLocationPermissionGranted(action.isGranted)
            // RequestLocationPermission action removed: permission requests are initiated from the UI
            // via rememberMultiplePermissionsState.launchPermissionRequest() and resolved via ActivityResult.
        }
    }

    private fun handleSearch(stationName: String) {
        searchJob?.cancel()
        _uiState.update {
            it.copy(
                filterStationName = stationName,
                currentPage = 0,
                stations = persistentListOf(),
                hasMorePages = true
            )
        }
        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MS)
            if (stationName.isNotBlank()) stationListTracker.trackSearchSubmitted(stationName)
            fetchRadioStations(force = true)
        }
    }

    private fun handleClick(station: UiState.Station) {
        if (radioPlayerController?.currentMediaId == station.serverUuid) {
            if (radioPlayerController?.isPlaying == true) {
                stationListTracker.trackPlaybackPaused(station.serverUuid)
                radioPlayerController?.pause()
            } else {
                stationListTracker.trackPlaybackResumed(station.serverUuid)
                radioPlayerController?.play()
            }
        } else {
            stationListTracker.trackStationSelected(station.serverUuid, station.name)
            radioPlayerController?.apply {
                setMediaItem(station.toRadioMediaItem())
                prepare()
                play()
            }
            updatePlayerColors(station.imageUrl)
        }
    }

    private fun handleLocationPermissionGranted(isGranted: Boolean) {
        _uiState.update {
            it.copy(
                showLocationPermissionSheet = false,
                locationPermissionResolved = true
            )
        }
        if (!isGranted) {
            fetchRadioStations()
            return
        }
        viewModelScope.launch {
            val location = locationProvider.getCurrentLocation()
            if (location == null) {
                fetchRadioStations()
                return@launch
            }
            _uiState.update {
                it.copy(
                    locationName = listOf(location.adminArea, location.city)
                        .filter { it.isNotEmpty() }
                        .distinct()
                        .joinToString(", ")
                        .takeIf { it.isNotEmpty() },
                    currentPosition = GeoLatLong(location.latitude, location.longitude),
                    currentPage = 0,
                    stations = persistentListOf(),
                    hasMorePages = true,
                )
            }
            fetchRadioStations(force = true)
        }
    }

    private fun trackPlayerEvent(source: String, expanded: Boolean) {
        val s = _uiState.value.selectedStation
        if (expanded) stationListTracker.trackPlayerExpanded(source, s?.serverUuid, s?.name, s?.isPlaying == true)
        else stationListTracker.trackPlayerCollapsed(source, s?.serverUuid, s?.name, s?.isPlaying == true)
    }

    private fun fetchRadioStations(force: Boolean = false) {
        if (!force && fetchJob?.isActive == true) return
        if (!_uiState.value.hasMorePages) return

        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val nextPage = _uiState.value.currentPage + 1
                val fetchedStations = getRadioStationsUseCase.execute(
                    page = nextPage,
                    searchName = _uiState.value.filterStationName,
                    location = _uiState.value.currentPosition,
                ).map {
                    val isCurrentlyPlaying = (radioPlayerController?.currentMediaId == it.stationUuid)
                            && (radioPlayerController?.isPlaying == true)
                    it.toUiStateStation().copy(isPlaying = isCurrentlyPlaying)
                }.toPersistentList()
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
    }

    private fun onPlaybackEventReceived(playbackEvent: PlaybackEvent) {
        val station = _uiState.value.selectedStation
        when (playbackEvent) {
            is PlaybackEvent.PlayingChanged -> _uiState.update {
                it.copy(
                    selectedStation = station?.copy(
                        isPlaying = playbackEvent.isPlaying,
                        isBuffering = false
                    ),
                    stations = it.stations.togglePlayingStateForStations(
                        targetUuid = radioPlayerController?.currentMediaId.orEmpty(),
                        isPlaying = playbackEvent.isPlaying
                    ))
            }
            is PlaybackEvent.StateChanged -> _uiState.update {
                it.copy(selectedStation = station?.copy(isBuffering = playbackEvent.state == PlaybackState.BUFFERING))
            }
            is PlaybackEvent.MediaItemTransition -> playbackEvent.mediaId?.let { mediaId ->
                viewModelScope.launch {
                    val s = getRadioStationUseCase.execute(mediaId)
                    val uiStation = s.toUiStateStation().copy(
                        isPlaying = radioPlayerController?.isPlaying == true,
                        isBuffering = station?.isBuffering ?: false
                    )
                    _uiState.update { it.copy(selectedStation = uiStation) }
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

    data class UiState(
        val filterStationName: String? = null,
        val currentPage: Int = 0,
        val stations: ImmutableList<Station> = persistentListOf(),
        val selectedStation: Station? = null,
        val hasMorePages: Boolean = true,
        val isLoading: Boolean = true,
        val playerColors: PlayerColors = PlayerColors(
            Color(0xFF1C1A24),
            Color(0xFF3a1040),
            Color(0xFF0e0c14)
        ),
        val featureFlag: FeatureFlag = FeatureFlag(),
        val locationName: String? = null,
        val currentPosition: GeoLatLong? = null,
        val locationPermissionResolved: Boolean = false,
        val showLocationPermissionSheet: Boolean = true,
    ) {
        data class Station(
            val serverUuid: String,
            val name: String,
            val genre: String,
            val imageUrl: String,
            val streamUrl: String,
            val isBuffering: Boolean,
            val isPlaying: Boolean
        )
        data class FeatureFlag(
            val isMoreMenuEnabled: Boolean = false,
            val isFavoriteEnabled: Boolean = false,
            val isActionRowEnabled: Boolean = false
        )
    }

    sealed interface Action {
        data object LoadMore : Action
        data class Search(val stationName: String) : Action
        data class Click(val station: UiState.Station) : Action
        data class Play(val station: UiState.Station) : Action
        data class Pause(val station: UiState.Station) : Action
        data class Stop(val station: UiState.Station) : Action
        data class ExpandPlayer(val source: String) : Action
        data class CollapsePlayer(val source: String) : Action
        data class OnLocationPermissionGranted(val isGranted: Boolean) : Action
        // RequestLocationPermission removed — kept permission flow in the UI layer
    }

    companion object {
        internal const val SEARCH_DEBOUNCE_MS = 300L
        internal const val PAGE_SIZE = 10
    }
}

private fun RadioStation.toUiStateStation() = StationListViewModel.UiState.Station(
    serverUuid = stationUuid,
    name = name,
    genre = tags.getOrNull(0).orEmpty(),
    imageUrl = imageUrl,
    streamUrl = url,
    isBuffering = false,
    isPlaying = false
)

private fun StationListViewModel.UiState.Station.toRadioMediaItem() = RadioMediaItem(
    mediaId = serverUuid,
    streamUrl = streamUrl,
    radioMetadata = RadioMediaItem.RadioMetadata(name, genre, imageUrl)
)

private fun ImmutableList<StationListViewModel.UiState.Station>.togglePlayingStateForStations(
    targetUuid: String,
    isPlaying: Boolean
): ImmutableList<StationListViewModel.UiState.Station> {
    return map { s ->
        if (s.serverUuid == targetUuid) {
            s.copy(isPlaying = isPlaying)
        } else {
            s.copy(isPlaying = false)
        }
    }.toPersistentList()
}
