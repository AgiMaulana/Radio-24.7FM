package io.github.agimaulana.radio.core.radioplayer.internal

import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import io.github.agimaulana.radio.domain.api.entity.GeoLatLong
import io.github.agimaulana.radio.domain.api.entity.RadioStation
import io.github.agimaulana.radio.domain.api.repository.CatalogState
import io.github.agimaulana.radio.domain.api.repository.CatalogStateRepository
import io.github.agimaulana.radio.domain.api.usecase.GetPinnedStationsUseCase
import io.github.agimaulana.radio.domain.api.usecase.GetRadioStationUseCase
import io.github.agimaulana.radio.domain.api.usecase.GetRadioStationsUseCase
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.flow.first
import timber.log.Timber

internal class RadioLibraryCatalog(
    private val getRadioStationsUseCase: GetRadioStationsUseCase,
    private val getPinnedStationsUseCase: GetPinnedStationsUseCase,
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
                    .setIsPlayable(false)
                    .setTitle("Radio 24.7FM")
                    .build()
            )
            .build()
    }

    fun pinnedItem(): MediaItem {
        return browsableCategoryItem(
            mediaId = PINNED_MEDIA_ID,
            title = "Pinned"
        )
    }

    fun stationsItem(): MediaItem {
        return browsableCategoryItem(
            mediaId = STATIONS_MEDIA_ID,
            title = "Stations"
        )
    }

    suspend fun getPinned(): List<MediaItem> {
        return getPinnedStationsUseCase.execute()
            .first()
            .map { it.toMediaItem() }
    }

    suspend fun getStations(
        page: Int,
        pageSize: Int,
        search: String? = null,
        location: GeoLatLong? = null,
    ): List<MediaItem> {
        if (pageSize <= 0) return emptyList()

        val startIndex = page * pageSize
        val firstCatalogPage = startIndex / CATALOG_PAGE_SIZE + 1
        val lastCatalogPage = (startIndex + pageSize - 1) / CATALOG_PAGE_SIZE + 1
        val stations = mutableListOf<RadioStation>()

        for (catalogPage in firstCatalogPage..lastCatalogPage) {
            val pageStations = loadStationsPage(
                page = catalogPage,
                search = search,
                location = location
            )
            if (pageStations.isEmpty()) break
            stations.addAll(pageStations)
        }

        val offsetInStations = startIndex - (firstCatalogPage - 1) * CATALOG_PAGE_SIZE
        return stations.drop(offsetInStations).take(pageSize).map { it.toMediaItem() }
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

    suspend fun getCurrentPage(): Int {
        return currentState().page
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
        val streamUri = resolvedUrl.ifEmpty { url }
        if (streamUri.isBlank()) {
            Timber.tag("RadioLibraryCatalog").w("Radio station %s has no stream URL", name)
        }

        return MediaItem.Builder()
            .setMediaId(stationUuid)
            .setUri(streamUri.takeIf { it.isNotBlank() }?.toUri())
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setIsBrowsable(false)
                    .setIsPlayable(streamUri.isNotBlank())
                    .setTitle(name)
                    .setSubtitle(tags.firstOrNull().orEmpty())
                    .setArtworkUri(imageUrl.takeIf { it.isNotBlank() }?.toUri())
                    .build()
            )
            .build()
    }

    private suspend fun loadStationsPage(
        page: Int,
        search: String?,
        location: GeoLatLong?,
    ): List<RadioStation> {
        return getRadioStationsUseCase.execute(
            page = page,
            searchName = search?.takeIf { it.isNotBlank() },
            location = location
        )
    }

    private suspend fun loadStationsPage(page: Int, state: CatalogState): List<RadioStation> {
        return when (state.source) {
            CatalogState.Source.ALL -> loadStationsPage(
                page = page,
                search = null,
                location = null
            )

            CatalogState.Source.SEARCH -> loadStationsPage(
                page = page,
                search = state.query,
                location = null
            )

            CatalogState.Source.LOCATION -> loadStationsPage(
                page = page,
                search = null,
                location = state.toLocation()
            )
        }
    }

    private fun browsableCategoryItem(
        mediaId: String,
        title: String,
    ): MediaItem {
        return MediaItem.Builder()
            .setMediaId(mediaId)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setIsBrowsable(true)
                    .setIsPlayable(false)
                    .setTitle(title)
                    .build()
            )
            .build()
    }

    companion object {
        internal const val ROOT_MEDIA_ID = "root"
        internal const val PINNED_MEDIA_ID = "pinned"
        internal const val STATIONS_MEDIA_ID = "stations"
        internal const val CATALOG_PAGE_SIZE = 10
        private const val MAX_INITIAL_PAGES = 3
    }
}
