package io.github.agimaulana.radio.core.radioplayer.internal

import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import io.github.agimaulana.radio.domain.api.entity.RadioStation
import io.github.agimaulana.radio.domain.api.usecase.GetRadioStationsUseCase

internal class RadioLibraryCatalog(
    private val getRadioStationsUseCase: GetRadioStationsUseCase,
) {
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

    suspend fun loadChildren(): List<MediaItem> {
        val stations = getRadioStationsUseCase.execute(
            page = 1,
            searchName = null,
            location = null
        )
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
