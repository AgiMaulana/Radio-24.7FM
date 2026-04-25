package io.github.agimaulana.radio.domain.api.repository

import io.github.agimaulana.radio.domain.api.entity.GeoLatLong
import io.github.agimaulana.radio.domain.api.entity.RadioStation

interface RadioStationRepository {
    suspend fun getRadioStations(
        location: GeoLatLong? = null,
        distance: Int? = null,
        country: String? = null,
        offset: Int,
        limit: Int
    ): List<RadioStation>

    suspend fun getRadioStationsByCountry(offset: Int, limit: Int): List<RadioStation>
    suspend fun searchRadioStations(searchQuery: String, offset: Int, limit: Int): List<RadioStation>
    suspend fun getRadioStation(stationUuid: String): RadioStation?
}
