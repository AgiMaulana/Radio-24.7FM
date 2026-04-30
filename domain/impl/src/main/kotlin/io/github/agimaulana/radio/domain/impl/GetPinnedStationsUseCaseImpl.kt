package io.github.agimaulana.radio.domain.impl

import io.github.agimaulana.radio.domain.api.entity.RadioStation
import io.github.agimaulana.radio.domain.api.repository.PinnedStationRepository
import io.github.agimaulana.radio.domain.api.usecase.GetPinnedStationsUseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPinnedStationsUseCaseImpl @Inject constructor(
    private val repository: PinnedStationRepository
) : GetPinnedStationsUseCase {
    override fun execute(): Flow<List<RadioStation>> {
        return repository.getPinnedStations()
    }
}
