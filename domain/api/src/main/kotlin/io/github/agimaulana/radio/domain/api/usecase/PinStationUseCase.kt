package io.github.agimaulana.radio.domain.api.usecase

import io.github.agimaulana.radio.domain.api.entity.RadioStation

interface PinStationUseCase {
    suspend fun execute(station: RadioStation)
}
