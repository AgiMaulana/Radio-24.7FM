package io.github.agimaulana.radio.domain.api.usecase

interface UnpinStationUseCase {
    suspend fun execute(stationUuid: String)
}
