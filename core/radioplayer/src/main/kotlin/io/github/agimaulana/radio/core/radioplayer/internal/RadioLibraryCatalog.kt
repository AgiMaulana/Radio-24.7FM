package io.github.agimaulana.radio.core.radioplayer.internal

import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import io.github.agimaulana.radio.domain.api.entity.GeoLatLong
import io.github.agimaulana.radio.domain.api.entity.RadioStation
import io.github.agimaulana.radio.domain.api.repository.CatalogState
import io.github.agimaulana.radio.domain.api.repository.CatalogStateRepository
import io.github.agimaulana.radio.domain.api.usecase.GetRadioStationUseCase
import io.github.agimaulana.radio.domain.api.usecase.GetRadioStationsUseCase
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class RadioLibraryCatalog(
    private val getRadioStationsUseCase: GetRadioStationsUseCase,
    private val getRadioStationUseCase: GetRadioStationUseCase,
    private val catalogStateRepository: CatalogStateRepository,
) {
    @Volatile private var cachedChildren: List<MediaItem>? = null
    private var restoredState: CatalogState = CatalogState()
    @Volatile private var hasRestoredState = false
    private val stateMutex = Mutex()
    private val cacheMutex = Mutex()

    fun rootItem(): MediaItem {
        return MediaItem.Builder()
            .setMediaId(ROOT_MEDIA_ID)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setIsBrowsable(true)
                    .setTitle("Radio 24.7FM")
                    .build()
            )
            .build()
    }

    suspend fun loadChildren(page: Int, pageSize: Int): List<MediaItem> {
        restore()
        updateState { it.copy(page = page) }

        val startIndex = page * pageSize
        if (pageSize <= 0) return emptyList()

        val state = currentState()
        val firstCatalogPage = startIndex / CATALOG_PAGE_SIZE + 1
        val lastCatalogPage = (startIndex + pageSize - 1) / CATALOG_PAGE_SIZE + 1
        val stations = mutableListOf<RadioStation>()

        for (catalogPage in firstCatalogPage..lastCatalogPage) {
            val pageStations = loadStationsPage(catalogPage, state)
            if (pageStations.isEmpty()) break
            stations.addAll(pageStations)
        }

        val offsetInStations = startIndex - (firstCatalogPage - 1) * CATALOG_PAGE_SIZE
        return stations.drop(offsetInStations).take(pageSize).map { it.toMediaItem() }
    }

    suspend fun findChild(mediaId: String): MediaItem? {
        restore()
        if (mediaId == ROOT_MEDIA_ID) return rootItem()

        val allChildren = getPlaylist()
        val cached = allChildren.firstOrNull { it.mediaId == mediaId }

        return cached ?: run {
            try {
                getRadioStationUseCase.execute(mediaId).toMediaItem()
            } catch (_: NoSuchElementException) {
                null
            } catch (_: Exception) {
                null
            }
        }
    }

    suspend fun getPlaylist(): List<MediaItem> {
        restore()
        return cachedChildren ?: cacheMutex.withLock {
            cachedChildren ?: loadInitialChildren().also { cachedChildren = it }
        }
    }

    suspend fun restore() {
        ensureRestored()
    }

    suspend fun loadInitial(
        query: String? = null,
        location: GeoLatLong? = null,
        source: CatalogState.Source = CatalogState.Source.ALL,
    ) {
        updateState {
            it.copy(
                query = query,
                locationLat = location?.latitude,
                locationLon = location?.longitude,
                page = 0,
                source = source,
            )
        }
    }

    suspend fun loadNextPage() {
        updateState { it.copy(page = it.page + 1) }
    }

    suspend fun updateLocation(location: GeoLatLong?) {
        updateState {
            it.copy(
                locationLat = location?.latitude,
                locationLon = location?.longitude,
                source = CatalogState.Source.LOCATION,
            )
        }
    }

    private suspend fun ensureRestored() {
        if (hasRestoredState) return
        stateMutex.withLock {
            if (hasRestoredState) return
            restoredState = catalogStateRepository.load() ?: restoredState
            hasRestoredState = true
        }
    }

    private suspend fun currentState(): CatalogState {
        ensureRestored()
        return stateMutex.withLock { restoredState }
    }

    private suspend fun updateState(transform: (CatalogState) -> CatalogState) {
        ensureRestored()
        val state = stateMutex.withLock {
            val previousState = restoredState
            val updatedState = transform(restoredState)
            restoredState = updatedState
            if (shouldInvalidateCache(previousState, updatedState)) {
                cachedChildren = null
            }
            restoredState
        }
        catalogStateRepository.save(state)
    }

    private fun shouldInvalidateCache(previousState: CatalogState, updatedState: CatalogState): Boolean {
        return previousState.query != updatedState.query ||
            previousState.locationLat != updatedState.locationLat ||
            previousState.locationLon != updatedState.locationLon ||
            previousState.source != updatedState.source
    }

    private suspend fun loadInitialChildren(): List<MediaItem> {
        val state = currentState()
        val stations = mutableListOf<RadioStation>()
        var nextPage = 1

        while (nextPage <= MAX_INITIAL_PAGES) {
            val pageStations = loadStationsPage(nextPage, state)
            if (pageStations.isEmpty()) break

            stations.addAll(pageStations)
            nextPage++
        }

        return stations.map { it.toMediaItem() }
    }

    private fun RadioStation.toMediaItem(): MediaItem {
        return MediaItem.Builder()
            .setMediaId(stationUuid)
            .setUri(resolvedUrl.ifEmpty { url })
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setIsPlayable(true)
                    .setTitle(name)
                    .setSubtitle(tags.firstOrNull().orEmpty())
                    .setArtworkUri(imageUrl.takeIf { it.isNotBlank() }?.toUri())
                    .build()
            )
            .build()
    }

    private suspend fun loadStationsPage(page: Int, state: CatalogState): List<RadioStation> {
        return when (state.source) {
            CatalogState.Source.ALL -> getRadioStationsUseCase.execute(
                page = page,
                searchName = null,
                location = null
            )

            CatalogState.Source.SEARCH -> getRadioStationsUseCase.execute(
                page = page,
                searchName = state.query?.takeIf { it.isNotBlank() },
                location = null
            )

            CatalogState.Source.LOCATION -> getRadioStationsUseCase.execute(
                page = page,
                searchName = null,
                location = state.toLocation()
            )
        }
    }

    companion object {
        internal const val ROOT_MEDIA_ID = "root"
        internal const val CATALOG_PAGE_SIZE = 10
        private const val MAX_INITIAL_PAGES = 3
    }
}
