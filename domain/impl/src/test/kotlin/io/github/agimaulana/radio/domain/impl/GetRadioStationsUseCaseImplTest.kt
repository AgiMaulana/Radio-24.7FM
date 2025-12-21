package io.github.agimaulana.radio.domain.impl

import io.github.agimaulana.radio.domain.api.repository.RadioStationRepository
import io.github.agimaulana.radio.domain.api.usecase.GetRadioStationsUseCase
import io.github.agimaulana.radio.domain.impl.datafactories.newRadioStation
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import kotlin.random.Random

class GetRadioStationsUseCaseImplTest {

    @RelaxedMockK
    private lateinit var radioStationRepository: RadioStationRepository

    private lateinit var useCase: GetRadioStationsUseCase

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        useCase = GetRadioStationsUseCaseImpl(radioStationRepository)
    }

    @Test
    fun `when execute then fetch radio stations from repository`() = runTest {
        val page = Random.nextInt()
        val radioStations = listOf(
            newRadioStation(
                withStationUuid = "uuid",
                withName = "24.7 FM",
                withTags = listOf("Pop"),
                withImageUrl = "https://image.com/123.png",
            )
        )
        coEvery {
            radioStationRepository.getRadioStations(page)
        } returns radioStations

        val result = useCase.execute(page)

        coVerify(exactly = 1) {
            radioStationRepository.getRadioStations(page)
        }
        assertEquals(radioStations, result)
    }
}
