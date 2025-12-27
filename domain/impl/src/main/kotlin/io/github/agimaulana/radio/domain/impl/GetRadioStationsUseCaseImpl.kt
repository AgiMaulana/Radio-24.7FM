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
        return if (searchName.isNullOrEmpty()) {
            radioStationRepository.getRadioStations(page)
        } else {
            radioStationRepository.getRadioStations(searchName, page)
        }
    }
}
