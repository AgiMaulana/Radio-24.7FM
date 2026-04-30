package io.github.agimaulana.radio.domain.api.repository

import io.github.agimaulana.radio.domain.api.entity.RadioStation
import kotlinx.coroutines.flow.Flow

interface PinnedStationRepository {
    fun getPinnedStations(): Flow<List<RadioStation>>
    suspend fun pinStation(station: RadioStation)
    suspend fun unpinStation(stationUuid: String)
    suspend fun isPinned(stationUuid: String): Boolean
}
