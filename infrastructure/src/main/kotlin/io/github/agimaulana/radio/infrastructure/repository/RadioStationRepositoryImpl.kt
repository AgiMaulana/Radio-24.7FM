package io.github.agimaulana.radio.infrastructure.repository

import io.github.agimaulana.radio.core.common.DispatcherProvider
import io.github.agimaulana.radio.domain.api.entity.RadioStation
import io.github.agimaulana.radio.domain.api.repository.RadioStationRepository
import io.github.agimaulana.radio.infrastructure.api.RadioStationApi
import io.github.agimaulana.radio.infrastructure.response.RadioStationResponse
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RadioStationRepositoryImpl @Inject constructor(
    private val radioStationApi: RadioStationApi,
    private val dispatcherProvider: DispatcherProvider,
) : RadioStationRepository {

    override suspend fun getRadioStations(offset: Int, limit: Int): List<RadioStation> {
        return withContext(dispatcherProvider.io) {
            radioStationApi.getRadioStations(offset = offset, limit = limit).map { it.toEntity() }
        }
    }

    override suspend fun searchRadioStations(
        searchQuery: String,
        offset: Int,
        limit: Int
    ): List<RadioStation> {
        return withContext(dispatcherProvider.io) {
            radioStationApi.searchRadioStations(name = searchQuery, offset = offset, limit = limit)
                .map { it.toEntity() }
        }
    }

    override suspend fun getRadioStation(stationUuid: String): RadioStation? {
        return withContext(dispatcherProvider.io) {
            radioStationApi.getRadioStationByUuid(format = "json", uuid = stationUuid)
                .map { it.toEntity() }
                .firstOrNull { it.stationUuid == stationUuid }
        }
    }

    private fun RadioStationResponse.toEntity(): RadioStation {
        return RadioStation(
            stationUuid = this.stationuuid,
            name = this.name,
            imageUrl = this.favicon,
            tags = this.tags.split(","),
            url = this.url,
            resolvedUrl = this.urlResolved
        )
    }
}
