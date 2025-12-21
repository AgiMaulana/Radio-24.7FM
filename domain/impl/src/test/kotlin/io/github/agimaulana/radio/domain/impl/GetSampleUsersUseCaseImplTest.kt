package io.github.agimaulana.radio.domain.impl

import io.github.agimaulana.radio.domain.api.repository.SampleUserRepository
import io.github.agimaulana.radio.domain.impl.GetSampleUsersUseCaseImpl
import io.github.agimaulana.radio.domain.impl.datafactories.newSampleUser
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetSampleUsersUseCaseImplTest {

    @RelaxedMockK
    private lateinit var sampleUserRepository: SampleUserRepository

    private lateinit var useCase: GetSampleUsersUseCaseImpl

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        useCase = GetSampleUsersUseCaseImpl(sampleUserRepository)
    }

    @Test
    fun `when execute then get sample users from repository`() = runTest {
        val sampleUsers = listOf(
            newSampleUser(withId = "1", withName = "User 1"),
        )
        coEvery {
            sampleUserRepository.getSampleUsers()
        } returns sampleUsers

        val result = useCase.execute()

        coVerify { sampleUserRepository.getSampleUsers() }
        assertEquals(sampleUsers, result)
    }
}