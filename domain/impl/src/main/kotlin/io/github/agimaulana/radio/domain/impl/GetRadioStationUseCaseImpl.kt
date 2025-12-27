package io.github.agimaulana.radio.domain.impl

import io.github.agimaulana.radio.domain.api.entity.RadioStation
import io.github.agimaulana.radio.domain.api.repository.RadioStationRepository
import io.github.agimaulana.radio.domain.api.usecase.GetRadioStationUseCase
import javax.inject.Inject

class GetRadioStationUseCaseImpl @Inject constructor(
    private val radioStationRepository: RadioStationRepository
) : GetRadioStationUseCase {
    override suspend fun execute(stationUuid: String): RadioStation {
        return radioStationRepository.getRadioStation(stationUuid)
    }
}
