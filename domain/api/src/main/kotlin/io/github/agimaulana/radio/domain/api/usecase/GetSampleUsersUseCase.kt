package io.github.agimaulana.radio.domain.api.usecase

import io.github.agimaulana.radio.domain.api.entity.SampleUser

interface GetSampleUsersUseCase {
    suspend fun execute(): List<SampleUser>
}