package io.github.agimaulana.radio.domain.api.repository

import io.github.agimaulana.radio.domain.api.entity.RadioStation

interface RadioStationRepository {
    suspend fun getRadioStations(page: Int): List<RadioStation>
    suspend fun getRadioStations(searchQuery: String, page: Int): List<RadioStation>
    suspend fun getRadioStation(stationUuid: String): RadioStation
}
