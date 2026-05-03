package io.github.agimaulana.radio.core.radioplayer.internal

import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import io.github.agimaulana.radio.domain.api.entity.RadioStation
import io.github.agimaulana.radio.domain.api.usecase.GetRadioStationsUseCase

internal class RadioLibraryCatalog(
    private val getRadioStationsUseCase: GetRadioStationsUseCase,
) {
    private var cachedChildren: List<MediaItem>? = null

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
        val startIndex = page * pageSize
        if (pageSize <= 0) return emptyList()

        val firstCatalogPage = startIndex / CATALOG_PAGE_SIZE + 1
        val lastCatalogPage = (startIndex + pageSize - 1) / CATALOG_PAGE_SIZE + 1
        val stations = mutableListOf<RadioStation>()

        for (catalogPage in firstCatalogPage..lastCatalogPage) {
            val pageStations = loadStationsPage(catalogPage)
            if (pageStations.isEmpty()) break
            stations.addAll(pageStations)
        }

        val offsetInStations = startIndex - (firstCatalogPage - 1) * CATALOG_PAGE_SIZE
        return stations.drop(offsetInStations).take(pageSize).map { it.toMediaItem() }
    }

    suspend fun findChild(mediaId: String): MediaItem? {
        if (mediaId == ROOT_MEDIA_ID) return rootItem()

        val allChildren = cachedChildren ?: loadAllChildren().also { cachedChildren = it }
        return allChildren.firstOrNull { it.mediaId == mediaId }
    }

    private suspend fun loadAllChildren(): List<MediaItem> {
        val stations = mutableListOf<RadioStation>()
        var nextPage = 1

        while (true) {
            val pageStations = getRadioStationsUseCase.execute(
                page = nextPage,
                searchName = null,
                location = null
            )
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

    private suspend fun loadStationsPage(page: Int): List<RadioStation> {
        return getRadioStationsUseCase.execute(
            page = page,
            searchName = null,
            location = null
        )
    }

    companion object {
        internal const val ROOT_MEDIA_ID = "root"
        private const val CATALOG_PAGE_SIZE = 10
    }
}
