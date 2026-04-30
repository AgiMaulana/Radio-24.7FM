package io.github.agimaulana.radio.domain.impl

import io.github.agimaulana.radio.domain.api.repository.PinnedStationRepository
import io.github.agimaulana.radio.domain.api.usecase.UnpinStationUseCase
import javax.inject.Inject

class UnpinStationUseCaseImpl @Inject constructor(
    private val repository: PinnedStationRepository
) : UnpinStationUseCase {
    override suspend fun execute(stationUuid: String) {
        repository.unpinStation(stationUuid)
    }
}
