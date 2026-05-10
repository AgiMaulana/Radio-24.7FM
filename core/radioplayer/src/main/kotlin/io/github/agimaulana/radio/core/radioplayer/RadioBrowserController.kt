package io.github.agimaulana.radio.core.radioplayer

import io.github.agimaulana.radio.domain.api.entity.GeoLatLong
import kotlinx.coroutines.flow.Flow

interface RadioBrowserController {
    val pinnedStations: Flow<List<RadioMediaItem>>

    suspend fun getPinned(): List<RadioMediaItem>

    suspend fun getStation(mediaId: String): RadioMediaItem?

    suspend fun getStations(
        page: Int,
        pageSize: Int,
        searchName: String? = null,
        location: GeoLatLong? = null,
    ): List<RadioMediaItem>

    fun release()
}
