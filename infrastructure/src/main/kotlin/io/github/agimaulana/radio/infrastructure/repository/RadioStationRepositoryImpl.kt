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

    override suspend fun getRadioStations(page: Int): List<RadioStation> {
        return withContext(dispatcherProvider.io) {
            radioStationApi.getRadioStations(page)
                .map { it.toEntity() }
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
