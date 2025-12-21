package io.github.agimaulana.radio.domain.impl

import io.github.agimaulana.radio.domain.api.entity.SampleUser
import io.github.agimaulana.radio.domain.api.repository.SampleUserRepository
import io.github.agimaulana.radio.domain.api.usecase.GetSampleUsersUseCase
import javax.inject.Inject

class GetSampleUsersUseCaseImpl @Inject constructor(
    private val sampleUserRepository: SampleUserRepository
) : GetSampleUsersUseCase {
    override suspend fun execute(): List<SampleUser> {
        return sampleUserRepository.getSampleUsers()
    }
}
