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
        val allChildren = cachedChildren ?: loadAllChildren().also { cachedChildren = it }
        val startIndex = page * pageSize
        if (startIndex >= allChildren.size) return emptyList()
        val endIndex = minOf(startIndex + pageSize, allChildren.size)
        return allChildren.subList(startIndex, endIndex)
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
                    .setArtworkUri(imageUrl.toUri())
                    .build()
            )
            .build()
    }

    companion object {
        internal const val ROOT_MEDIA_ID = "root"
    }
}
