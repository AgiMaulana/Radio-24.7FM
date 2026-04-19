package io.github.agimaulana.radio.domain.impl

import io.github.agimaulana.radio.domain.api.repository.RadioStationRepository
import io.github.agimaulana.radio.domain.api.usecase.GetRadioStationsUseCase
import io.github.agimaulana.radio.domain.impl.datafactories.newRadioStation
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

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
    fun `when execute page 1 with null searchName then call getRadioStations with offset 0`() = runTest {
        val page = 1
        val expectedOffset = 0
        val limit = 10
        val radioStations = listOf(
            newRadioStation(
                withStationUuid = "uuid",
                withName = "24.7 FM",
                withTags = listOf("Pop"),
                withImageUrl = "https://image.com/123.png",
            )
        )
        coEvery {
            radioStationRepository.getRadioStations(offset = expectedOffset, limit = limit)
        } returns radioStations

        val result = useCase.execute(page, null)

        assertEquals(radioStations, result)
    }

    @Test
    fun `when execute page 2 then call getRadioStations with offset 10`() = runTest {
        val page = 2
        val expectedOffset = 10
        val limit = 10
        val radioStations = listOf(
            newRadioStation(
                withStationUuid = "uuid-1",
                withName = "Station 11",
            )
        )
        coEvery {
            radioStationRepository.getRadioStations(offset = expectedOffset, limit = limit)
        } returns radioStations

        val result = useCase.execute(page, null)

        assertEquals(radioStations, result)
    }

    @Test
    fun `when execute with searchName and page 2 then call searchRadioStations with offset 10`() = runTest {
        val page = 2
        val expectedOffset = 10
        val limit = 10
        val searchQuery = "jazz"
        val radioStations = listOf(
            newRadioStation(
                withStationUuid = "uuid-1",
                withName = "Jazz FM",
            )
        )
        coEvery {
            radioStationRepository.searchRadioStations(searchQuery = searchQuery, offset = expectedOffset, limit = limit)
        } returns radioStations

        val result = useCase.execute(page, searchQuery)

        assertEquals(radioStations, result)
    }

    @Test
    fun `when execute then return stations from repository`() = runTest {
        val page = 1
        val radioStations = listOf(
            newRadioStation(withStationUuid = "uuid-1", withName = "Station 1"),
            newRadioStation(withStationUuid = "uuid-2", withName = "Station 2"),
        )
        coEvery {
            radioStationRepository.getRadioStations(offset = 0, limit = 10)
        } returns radioStations

        val result = useCase.execute(page, null)

        assertEquals(2, result.size)
        assertTrue(result.any { it.stationUuid == "uuid-1" })
        assertTrue(result.any { it.stationUuid == "uuid-2" })
    }
}