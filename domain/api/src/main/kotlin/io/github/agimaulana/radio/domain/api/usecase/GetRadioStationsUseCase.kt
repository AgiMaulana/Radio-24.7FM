package io.github.agimaulana.radio.domain.api.usecase

import io.github.agimaulana.radio.domain.api.entity.RadioStation

interface GetRadioStationsUseCase {
    suspend fun execute(page: Int): List<RadioStation>
}
