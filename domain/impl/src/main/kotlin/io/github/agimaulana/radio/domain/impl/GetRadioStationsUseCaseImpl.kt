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
        searchName: String?
    ): List<RadioStation> {
        val offset = (page - 1) * PAGE_SIZE
        return if (searchName.isNullOrEmpty()) {
            radioStationRepository.getRadioStations(offset = offset, limit = PAGE_SIZE)
        } else {
            radioStationRepository.searchRadioStations(searchQuery = searchName, offset = offset, limit = PAGE_SIZE)
        }
    }

    companion object {
        private const val PAGE_SIZE = 10
    }
}
