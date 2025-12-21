package io.github.agimaulana.radio.domain.api.repository

import io.github.agimaulana.radio.domain.api.entity.SampleUser

interface SampleUserRepository {
    suspend fun getSampleUsers(): List<SampleUser>
}
