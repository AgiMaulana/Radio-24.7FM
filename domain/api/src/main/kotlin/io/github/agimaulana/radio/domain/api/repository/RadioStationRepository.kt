package io.github.agimaulana.radio.domain.api.repository

import io.github.agimaulana.radio.domain.api.entity.RadioStation

interface RadioStationRepository {
    suspend fun getRadioStations(offset: Int, limit: Int): List<RadioStation>
    suspend fun searchRadioStations(searchQuery: String, offset: Int, limit: Int): List<RadioStation>
    suspend fun getRadioStation(stationUuid: String): RadioStation?
}
