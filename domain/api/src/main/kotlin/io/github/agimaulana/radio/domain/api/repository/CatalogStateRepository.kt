package io.github.agimaulana.radio.domain.api.repository

import io.github.agimaulana.radio.domain.api.entity.GeoLatLong

data class CatalogState(
    val query: String? = null,
    val locationLat: Double? = null,
    val locationLon: Double? = null,
    val page: Int = 0,
    val source: Source = Source.ALL,
) {
    enum class Source {
        ALL,
        SEARCH,
        LOCATION,
        PINNED,
    }

    fun toLocation(): GeoLatLong? {
        if (locationLat == null || locationLon == null) return null
        return GeoLatLong(locationLat, locationLon)
    }
}

interface CatalogStateRepository {
    suspend fun save(state: CatalogState)
    suspend fun load(): CatalogState?
}
