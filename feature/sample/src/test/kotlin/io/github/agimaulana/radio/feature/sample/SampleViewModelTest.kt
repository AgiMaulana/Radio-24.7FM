package io.github.agimaulana.radio.feature.sample

import app.cash.turbine.turbineScope
import io.github.agimaulana.radio.domain.api.entity.SampleUser
import io.github.agimaulana.radio.domain.api.usecase.GetSampleUsersUseCase
import io.github.agimaulana.radio.feature.sample.datafactories.newSampleUser
import io.github.agimaulana.radio.feature.sample.datafactories.newUiStateMember
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class SampleViewModelTest {

    @RelaxedMockK
    private lateinit var getSampleUsersUseCase: GetSampleUsersUseCase

    private lateinit var viewModel: SampleViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        viewModel = SampleViewModel(getSampleUsersUseCase)
    }

    @Test
    fun `when init then fetch sample users`() = runTest {
        turbineScope {
            val sampleUsers = listOf(
                newSampleUser(withName = "User 1", withEmail = "user1@androidacademy.ac.id"),
                newSampleUser(withName = "User 2", withEmail = "user2@androidacademy.ac.id"),
            )
            coEvery {
                getSampleUsersUseCase.execute()
            } returns sampleUsers
            val expectedMembers = persistentListOf(
                newUiStateMember(withName = "User 1", withEmail = "user1@androidacademy.ac.id"),
                newUiStateMember(withName = "User 2", withEmail = "user2@androidacademy.ac.id"),
            )

            val uiState = viewModel.uiState.testIn(backgroundScope)

            assertEquals(persistentListOf<SampleUser>(), uiState.awaitItem().members)

            viewModel.init()

            assertEquals(expectedMembers, uiState.awaitItem().members)
        }
    }
}
