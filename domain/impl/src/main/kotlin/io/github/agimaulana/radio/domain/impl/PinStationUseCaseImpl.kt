package io.github.agimaulana.radio.domain.impl

import io.github.agimaulana.radio.domain.api.entity.RadioStation
import io.github.agimaulana.radio.domain.api.repository.PinnedStationRepository
import io.github.agimaulana.radio.domain.api.usecase.PinStationUseCase
import javax.inject.Inject

class PinStationUseCaseImpl @Inject constructor(
    private val repository: PinnedStationRepository
) : PinStationUseCase {
    override suspend fun execute(station: RadioStation) {
        repository.pinStation(station)
    }
}
