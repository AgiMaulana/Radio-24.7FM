package io.github.agimaulana.radio.feature.stationlist

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.common.api.ResolvableApiException
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.agimaulana.radio.core.radioplayer.PlaybackEvent
import io.github.agimaulana.radio.core.radioplayer.PlaybackState
import io.github.agimaulana.radio.core.radioplayer.RadioBrowserController
import io.github.agimaulana.radio.core.radioplayer.RadioBrowserFactory
import io.github.agimaulana.radio.core.radioplayer.RadioMediaItem
import io.github.agimaulana.radio.core.radioplayer.RadioPlayerController
import io.github.agimaulana.radio.core.radioplayer.RadioPlayerControllerFactory
import io.github.agimaulana.radio.domain.api.entity.GeoLatLong
import io.github.agimaulana.radio.domain.api.entity.RadioStation
import io.github.agimaulana.radio.domain.api.repository.PinnedStationLimitReachedException
import io.github.agimaulana.radio.domain.api.usecase.PinStationUseCase
import io.github.agimaulana.radio.domain.api.usecase.UnpinStationUseCase
import io.github.agimaulana.radio.feature.stationlist.location.LocationProvider
import io.github.agimaulana.radio.feature.stationlist.player.PlayerColors
import io.github.agimaulana.radio.feature.stationlist.player.extractPlayerColors
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@Suppress("TooManyFunctions", "LongParameterList")
@HiltViewModel
class StationListViewModel @Inject constructor(
    private val pinStationUseCase: PinStationUseCase,
    private val unpinStationUseCase: UnpinStationUseCase,
    private val radioPlayerControllerFactory: RadioPlayerControllerFactory,
    private val radioBrowserFactory: RadioBrowserFactory,
    private val stationListTracker: StationListTracker,
    private val locationProvider: LocationProvider,
    @ApplicationContext private val context: Context,
) : ViewModel() {
    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()
    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    private var radioPlayerController: RadioPlayerController? = null
    private var radioBrowser: RadioBrowserController? = null
    private var searchJob: Job? = null
    private var fetchJob: Job? = null
    private var pinnedStationsJob: Job? = null
    private var isStationsListNearEnd: Boolean? = null

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
            radioPlayerController = radioPlayerControllerFactory.get()
            radioBrowser = radioBrowserFactory.get()
            radioPlayerController?.apply {
                viewModelScope.launch { event.collect(::onPlaybackEventReceived) }
            }
            radioBrowser?.let(::observePinnedStations)
            restoreSelectedStation()
        }
    }

    private fun observePinnedStations(browser: RadioBrowserController) {
        pinnedStationsJob?.cancel()
        pinnedStationsJob = viewModelScope.launch {
            browser.pinnedStations.collect { browserPinned ->
                val pinnedUuids = browserPinned.map { it.mediaId }.toSet()
                val currentMediaId = radioPlayerController?.currentMediaId
                val isPlaying = radioPlayerController?.isPlaying == true
                _uiState.update { state ->
                    state.copy(
                        isPinnedStationsLoading = false,
                        pinnedStations = browserPinned.map {
                            it.toUiStateStation(pinnedUuids, currentMediaId, isPlaying)
                        }.toImmutableList(),
                        stations = state.stations.map {
                            it.copy(isPinned = it.serverUuid in pinnedUuids)
                        }.toPersistentList()
                    )
                }
            }
        }
    }

    private suspend fun restoreSelectedStation() {
        val mediaId = radioPlayerController?.currentMediaId ?: return
        if (mediaId.isNotEmpty()) {
            val browserStation = radioBrowser?.getStation(mediaId) ?: return
            val pinnedUuids = _uiState.value.pinnedStations.map { it.serverUuid }.toSet()
            val uiStation = browserStation.toUiStateStation(
                pinnedUuids = pinnedUuids,
                currentMediaId = mediaId,
                isPlaying = radioPlayerController?.isPlaying == true
            )
            _uiState.update { it.copy(selectedStation = uiStation) }
            updatePlayerColors(uiStation.imageUrl)
        }
    }

    fun onAction(action: Action) {
        when (action) {
            is Action.LoadMore -> {
                handleLoadMore(
                    totalItems = action.totalItems,
                    lastVisibleIndex = action.lastVisibleIndex
                )
            }

            is Action.Search -> handleSearch(action.stationName)

            is Action.Click -> handleClick(action.station)

            is Action.Pause -> {
                stationListTracker.trackPlaybackPaused(
                    stationId = _uiState.value.selectedStation?.serverUuid
                )
                radioPlayerController?.pause()
            }

            is Action.Play -> {
                val stationToPlay = action.station
                stationListTracker.trackPlaybackResumed(stationToPlay.serverUuid)

                if (radioPlayerController?.currentMediaId == stationToPlay.serverUuid) {
                    // already loaded; just resume
                    radioPlayerController?.play()
                } else {
                    val main = _uiState.value.stations
                    val startIndex = main.indexOfFirst {
                        it.serverUuid == stationToPlay.serverUuid
                    }

                    val context = _uiState.value.toPlaybackContext()
                    if (startIndex >= 0) {
                        val playlist = main.map { it.toRadioMediaItem() }
                        radioPlayerController?.apply {
                            startPlayback(
                                items = playlist,
                                startIndex = startIndex,
                                context = context
                            )
                        }
                    } else {
                        // Fallback to single item (pinned or not in list) —
                        // use startPlayback for metadata
                        radioPlayerController?.startPlayback(
                            items = listOf(stationToPlay.toRadioMediaItem()),
                            startIndex = 0,
                            context = context
                        )
                    }
                }
            }

            is Action.Stop -> {
                stationListTracker.trackPlaybackStopped(action.station.serverUuid)
                radioPlayerController?.stop()
                _uiState.update { it.copy(selectedStation = null) }
            }

            is Action.ExpandPlayer -> trackPlayerEvent(action.source, true)

            is Action.CollapsePlayer -> trackPlayerEvent(action.source, false)

            is Action.OnLocationPermissionGranted -> {
                handleLocationPermissionGranted(action.isGranted)
            }

            Action.OnLocationSettingsResolutionConsumed -> consumeLocationSettingsResolution()

            is Action.OnLocationSettingsResolved -> {
                handleLocationSettingsResolved(action.isResolved)
            }

            is Action.PinStation -> handlePinStation(action.station)

            is Action.UnpinStation -> handleUnpinStation(action.stationUuid)
        }
    }

    private fun handlePinStation(station: UiState.Station) {
        viewModelScope.launch {
            try {
                val domainStation = radioBrowser?.getStation(station.serverUuid)
                    ?.toRadioStation()
                    ?: station.toRadioStation()
                pinStationUseCase.execute(domainStation)
            } catch (exception: PinnedStationLimitReachedException) {
                _uiEvent.emit(UiEvent.ShowPinnedLimitReached(exception.maxPins))
            }
        }
    }

    private fun handleUnpinStation(stationUuid: String) {
        viewModelScope.launch {
            unpinStationUseCase.execute(stationUuid)
        }
    }

    private fun handleSearch(stationName: String) {
        searchJob?.cancel()
        isStationsListNearEnd = null
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
            if (stationName.isNotBlank()) {
                stationListTracker.trackSearchSubmitted(stationName)
            }
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
            stationListTracker.trackStationSelected(
                stationId = station.serverUuid,
                stationName = station.name
            )

            _uiState.update {
                it.copy(
                    selectedStation = station.copy(
                        isBuffering = true,
                        isPlaying = false
                    )
                )
            }

            val main = _uiState.value.stations
            val startIndex = main.indexOfFirst { it.serverUuid == station.serverUuid }

            val context = uiState.value.toPlaybackContext()
            if (startIndex >= 0) {
                val playlist = main.map { it.toRadioMediaItem() }
                radioPlayerController?.apply {
                    startPlayback(
                        items = playlist,
                        startIndex = startIndex,
                        context = context
                    )
                }
            } else {
                // Fallback: station not found in in-memory list (e.g., pinned or stale).
                // Play single item via startPlayback for metadata.
                radioPlayerController?.startPlayback(
                    items = listOf(station.toRadioMediaItem()),
                    startIndex = 0,
                    context = context
                )
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
            isStationsListNearEnd = null
            fetchRadioStations()
            return
        }
        viewModelScope.launch {
            val settingsResult = locationProvider.checkLocationSettings()
            if (settingsResult.isFailure) {
                val exception = settingsResult.exceptionOrNull()
                if (exception is ResolvableApiException) {
                    _uiState.update { it.copy(locationSettingsResolution = exception) }
                    return@launch
                }
            }

            val location = locationProvider.getCurrentLocation()
            if (location == null) {
                isStationsListNearEnd = null
                fetchRadioStations()
                return@launch
            }
            isStationsListNearEnd = null
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

    private fun handleLocationSettingsResolved(isResolved: Boolean) {
        consumeLocationSettingsResolution()
        if (isResolved) {
            handleLocationPermissionGranted(true)
        } else {
            isStationsListNearEnd = null
            fetchRadioStations()
        }
    }

    private fun handleLoadMore(totalItems: Int, lastVisibleIndex: Int) {
        if (totalItems <= 0) return

        val isNearEnd = lastVisibleIndex >= totalItems - STATIONS_LIST_NEAR_END_THRESHOLD
        val previousIsNearEnd = isStationsListNearEnd
        isStationsListNearEnd = isNearEnd

        if (isNearEnd && previousIsNearEnd == false) {
            stationListTracker.trackLoadMore(page = _uiState.value.currentPage + 1)
            fetchRadioStations()
        }
    }

    private fun consumeLocationSettingsResolution() {
        _uiState.update { it.copy(locationSettingsResolution = null) }
    }

    private fun trackPlayerEvent(source: String, expanded: Boolean) {
        val s = _uiState.value.selectedStation
        if (expanded) {
            stationListTracker.trackPlayerExpanded(
                source = source,
                stationId = s?.serverUuid,
                stationName = s?.name,
                isPlaying = s?.isPlaying == true
            )
        } else {
            stationListTracker.trackPlayerCollapsed(
                source = source,
                stationId = s?.serverUuid,
                stationName = s?.name,
                isPlaying = s?.isPlaying == true
            )
        }
    }

    private fun fetchRadioStations(force: Boolean = false) {
        if (!force && fetchJob?.isActive == true) return
        if (!_uiState.value.hasMorePages) return

        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            val browser = radioBrowser ?: run {
                _uiState.update { it.copy(isStationsLoading = false) }
                return@launch
            }
            _uiState.update { it.copy(isStationsLoading = true) }
            try {
                val nextPage = _uiState.value.currentPage + 1
                val browserPage = _uiState.value.currentPage
                val searchName = _uiState.value.filterStationName
                val location = _uiState.value.currentPosition
                val pinnedUuids = _uiState.value.pinnedStations.map { it.serverUuid }.toSet()
                val fetchedStations = browser.getStations(
                    page = browserPage,
                    pageSize = PAGE_SIZE,
                    searchName = searchName,
                    location = location
                ).orEmpty().map {
                    it.toUiStateStation(
                        pinnedUuids = pinnedUuids,
                        currentMediaId = radioPlayerController?.currentMediaId,
                        isPlaying = radioPlayerController?.isPlaying == true
                    )
                }.toPersistentList()
                _uiState.update {
                    it.copy(
                        currentPage = nextPage,
                        stations = (it.stations + fetchedStations).toPersistentList(),
                        hasMorePages = fetchedStations.isNotEmpty() && fetchedStations.size >= PAGE_SIZE,
                        isStationsLoading = false
                    )
                }
            } catch (e: Exception) {
                Timber.tag("StationListViewModel").e(e, "Error fetching radio stations")
                _uiState.update { it.copy(isStationsLoading = false) }
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
                    ),
                    pinnedStations = it.pinnedStations.togglePlayingStateForStations(
                        targetUuid = radioPlayerController?.currentMediaId.orEmpty(),
                        isPlaying = playbackEvent.isPlaying
                    ).toImmutableList()
                )
            }
            is PlaybackEvent.StateChanged -> _uiState.update {
                it.copy(
                    selectedStation = station?.copy(
                        isBuffering = playbackEvent.state == PlaybackState.BUFFERING
                    )
                )
            }
            is PlaybackEvent.MediaItemTransition -> playbackEvent.mediaId?.let { mediaId ->
                viewModelScope.launch {
                    val pinnedUuids = _uiState.value.pinnedStations.map { it.serverUuid }.toSet()
                    val browserStation = radioBrowser?.getStation(mediaId) ?: return@launch
                    val uiStation = browserStation.toUiStateStation(
                        pinnedUuids = pinnedUuids,
                        currentMediaId = mediaId,
                        isPlaying = radioPlayerController?.isPlaying == true
                    ).copy(
                        isPlaying = radioPlayerController?.isPlaying == true,
                        isBuffering = station?.isBuffering ?: false
                    )
                    _uiState.update { it.copy(selectedStation = uiStation) }
                    updatePlayerColors(uiStation.imageUrl)
                }
            }

            PlaybackEvent.PlaylistChanged -> {
                syncWithPlayer()
            }
        }
    }

    private fun syncWithPlayer() {
        val controller = radioPlayerController ?: return
        if (controller.getPlaylist().isEmpty()) return

        val currentMediaId = controller.currentMediaId
        val isPlaying = controller.isPlaying

        _uiState.update { state ->
            state.copy(
                stations = state.stations.togglePlayingStateForStations(
                    targetUuid = currentMediaId.orEmpty(),
                    isPlaying = isPlaying
                ),
                pinnedStations = state.pinnedStations.togglePlayingStateForStations(
                    targetUuid = currentMediaId.orEmpty(),
                    isPlaying = isPlaying
                ).toImmutableList()
            )
        }
    }

    private fun updatePlayerColors(imageUrl: String?) {
        viewModelScope.launch {
            val colors = extractPlayerColors(imageUrl, context)
            _uiState.update { it.copy(playerColors = colors) }
        }
    }

    private fun UiState.toPlaybackContext(): RadioPlayerController.PlaybackContext {
        val contextType = if (!filterStationName.isNullOrEmpty()) {
            RadioPlayerController.PlaybackContext.Type.SEARCH
        } else {
            RadioPlayerController.PlaybackContext.Type.DEFAULT
        }
        return RadioPlayerController.PlaybackContext(
            type = contextType,
            query = filterStationName
        )
    }

    data class UiState(
        val filterStationName: String? = null,
        val currentPage: Int = 0,
        val stations: ImmutableList<Station> = persistentListOf(),
        val pinnedStations: ImmutableList<Station> = persistentListOf(),
        val selectedStation: Station? = null,
        val hasMorePages: Boolean = true,
        val isPinnedStationsLoading: Boolean = true,
        val isStationsLoading: Boolean = true,
        val playerColors: PlayerColors = PlayerColors(
            Color(0xFF1C1A24),
            Color(0xFF3a1040),
            Color(0xFF0e0c14)
        ),
        val featureFlag: FeatureFlag = FeatureFlag(
            isFavoriteEnabled = true,
        ),
        val locationName: String? = null,
        val currentPosition: GeoLatLong? = null,
        val locationPermissionResolved: Boolean = false,
        val showLocationPermissionSheet: Boolean = true,
        val locationSettingsResolution: ResolvableApiException? = null,
    ) {
        data class Station(
            val serverUuid: String,
            val name: String,
            val genre: String,
            val imageUrl: String,
            val streamUrl: String,
            val isBuffering: Boolean,
            val isPlaying: Boolean,
            val isPinned: Boolean = false,
        )
        data class FeatureFlag(
            val isMoreMenuEnabled: Boolean = false,
            val isFavoriteEnabled: Boolean = false,
            val isActionRowEnabled: Boolean = false
        )
    }

    override fun onCleared() {
        super.onCleared()
        searchJob?.cancel()
        fetchJob?.cancel()
        pinnedStationsJob?.cancel()
        radioPlayerController?.release()
        radioBrowser?.release()
    }

    sealed interface Action {
        data class LoadMore(
            val totalItems: Int,
            val lastVisibleIndex: Int
        ) : Action
        data class Search(val stationName: String) : Action
        data class Click(val station: UiState.Station) : Action
        data class Play(val station: UiState.Station) : Action
        data class Pause(val station: UiState.Station) : Action
        data class Stop(val station: UiState.Station) : Action
        data class ExpandPlayer(val source: String) : Action
        data class CollapsePlayer(val source: String) : Action
        data class OnLocationPermissionGranted(val isGranted: Boolean) : Action
        data object OnLocationSettingsResolutionConsumed : Action
        data class OnLocationSettingsResolved(val isResolved: Boolean) : Action
        data class PinStation(val station: UiState.Station) : Action
        data class UnpinStation(val stationUuid: String) : Action
    }

    sealed interface UiEvent {
        data class ShowPinnedLimitReached(val maxPins: Int) : UiEvent
    }

    companion object {
        internal const val SEARCH_DEBOUNCE_MS = 300L
        internal const val PAGE_SIZE = 10
        private const val STATIONS_LIST_NEAR_END_THRESHOLD = 3
    }
}

private fun RadioStation.toUiStateStation(
    pinnedUuids: Set<String>
) = StationListViewModel.UiState.Station(
    serverUuid = stationUuid,
    name = name,
    genre = tags.getOrNull(0).orEmpty(),
    imageUrl = imageUrl,
    streamUrl = url,
    isBuffering = false,
    isPlaying = false,
    isPinned = stationUuid in pinnedUuids
)

private fun RadioMediaItem.toUiStateStation(
    pinnedUuids: Set<String>,
    currentMediaId: String?,
    isPlaying: Boolean
) = StationListViewModel.UiState.Station(
    serverUuid = mediaId,
    name = radioMetadata.stationName,
    genre = radioMetadata.genre,
    imageUrl = radioMetadata.imageUrl,
    streamUrl = streamUrl,
    isBuffering = false,
    isPlaying = isPlaying && (mediaId == currentMediaId),
    isPinned = mediaId in pinnedUuids
)

private fun StationListViewModel.UiState.Station.toRadioMediaItem() = RadioMediaItem(
    mediaId = serverUuid,
    streamUrl = streamUrl,
    radioMetadata = RadioMediaItem.RadioMetadata(name, genre, imageUrl)
)

private fun RadioMediaItem.toRadioStation() = RadioStation(
    stationUuid = mediaId,
    name = radioMetadata.stationName,
    tags = listOf(radioMetadata.genre).filter { it.isNotBlank() },
    imageUrl = radioMetadata.imageUrl,
    url = streamUrl,
    resolvedUrl = streamUrl,
)

private fun StationListViewModel.UiState.Station.toRadioStation() = RadioStation(
    stationUuid = serverUuid,
    name = name,
    tags = listOf(genre).filter { it.isNotBlank() },
    imageUrl = imageUrl,
    url = streamUrl,
    resolvedUrl = streamUrl,
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
