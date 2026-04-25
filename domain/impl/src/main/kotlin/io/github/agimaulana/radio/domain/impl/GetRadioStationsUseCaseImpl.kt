package io.github.agimaulana.radio.domain.impl

import io.github.agimaulana.radio.domain.api.entity.RadioStation
import io.github.agimaulana.radio.domain.api.repository.RadioStationRepository
import io.github.agimaulana.radio.domain.api.usecase.GetRadioStationsUseCase
import javax.inject.Inject

class GetRadioStationsUseCaseImpl @Inject constructor(
    private val radioStationRepository: RadioStationRepository
) : GetRadioStationsUseCase {
    override suspend fun execute(
        page: Int,
        searchName: String?,
        location: io.github.agimaulana.radio.domain.api.entity.GeoLatLong?
    ): List<RadioStation> {
        val offset = (page - 1) * PAGE_SIZE
        return if (searchName.isNullOrEmpty()) {
            if (location != null) {
                val tier2 = radioStationRepository.getRadioStations(
                    location = location,
                    distance = DISTANCE_TIER_2,
                    offset = offset,
                    limit = PAGE_SIZE
                )
                if (tier2.isEmpty()) {
                    radioStationRepository.getRadioStations(
                        location = location,
                        distance = DISTANCE_TIER_3,
                        offset = offset,
                        limit = PAGE_SIZE
                    )
                } else {
                    tier2
                }
            } else {
                radioStationRepository.getRadioStationsByCountry(
                    offset = offset,
                    limit = PAGE_SIZE
                )
            }
        } else {
            radioStationRepository.searchRadioStations(searchQuery = searchName, offset = offset, limit = PAGE_SIZE)
        }
    }

    companion object {
        private const val PAGE_SIZE = 10
        private const val DISTANCE_TIER_2 = 50_000
        private const val DISTANCE_TIER_3 = 150_000
    }
}
