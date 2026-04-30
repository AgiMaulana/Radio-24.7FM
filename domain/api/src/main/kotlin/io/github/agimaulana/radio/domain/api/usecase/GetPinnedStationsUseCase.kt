package io.github.agimaulana.radio.domain.api.usecase

import io.github.agimaulana.radio.domain.api.entity.RadioStation
import kotlinx.coroutines.flow.Flow

interface GetPinnedStationsUseCase {
    fun execute(): Flow<List<RadioStation>>
}
