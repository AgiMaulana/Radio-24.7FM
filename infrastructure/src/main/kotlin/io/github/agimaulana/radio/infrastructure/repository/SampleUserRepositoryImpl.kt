package io.github.agimaulana.radio.infrastructure.repository

import io.github.agimaulana.radio.domain.api.entity.SampleUser
import io.github.agimaulana.radio.domain.api.repository.SampleUserRepository
import javax.inject.Inject

class SampleUserRepositoryImpl @Inject constructor() : SampleUserRepository {
    override suspend fun getSampleUsers(): List<SampleUser> {
        TODO("Not yet implemented")
    }
}