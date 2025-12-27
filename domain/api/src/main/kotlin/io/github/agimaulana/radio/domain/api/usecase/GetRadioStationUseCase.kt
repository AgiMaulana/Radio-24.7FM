package io.github.agimaulana.radio.domain.api.usecase

import io.github.agimaulana.radio.domain.api.entity.RadioStation

interface GetRadioStationUseCase {
    suspend fun execute(stationUuid: String): RadioStation
}
