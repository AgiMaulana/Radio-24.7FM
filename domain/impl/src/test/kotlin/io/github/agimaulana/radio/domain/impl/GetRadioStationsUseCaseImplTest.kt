package io.github.agimaulana.radio.domain.impl

import io.github.agimaulana.radio.domain.api.entity.GeoLatLong
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
    fun `when execute with location and has stations in tier 2 then return tier 2 stations`() = runTest {
        val location = GeoLatLong(-6.2, 106.8)
        val stations = listOf(newRadioStation())
        coEvery {
            radioStationRepository.getRadioStations(location = location, distance = 50000, offset = 0, limit = 10)
        } returns stations

        val result = useCase.execute(page = 1, searchName = null, location = location)

        assertEquals(stations, result)
        coVerify(exactly = 1) {
            radioStationRepository.getRadioStations(location = location, distance = 50000, offset = 0, limit = 10)
        }
        coVerify(exactly = 0) {
            radioStationRepository.getRadioStations(location = location, distance = 150000, offset = 0, limit = 10)
        }
    }

    @Test
    fun `when execute with location and tier 2 is empty then fallback to tier 3`() = runTest {
        val location = GeoLatLong(-6.2, 106.8)
        val stations = listOf(newRadioStation())
        coEvery {
            radioStationRepository.getRadioStations(location = location, distance = 50000, offset = 0, limit = 10)
        } returns emptyList()
        coEvery {
            radioStationRepository.getRadioStations(location = location, distance = 150000, offset = 0, limit = 10)
        } returns stations

        val result = useCase.execute(page = 1, searchName = null, location = location)

        assertEquals(stations, result)
        coVerify {
            radioStationRepository.getRadioStations(location = location, distance = 50000, offset = 0, limit = 10)
            radioStationRepository.getRadioStations(location = location, distance = 150000, offset = 0, limit = 10)
        }
    }

    @Test
    fun `when execute without location then call getRadioStationsByCountry`() = runTest {
        val stations = listOf(newRadioStation())
        coEvery {
            radioStationRepository.getRadioStationsByCountry(offset = 0, limit = 10)
        } returns stations

        val result = useCase.execute(page = 1, searchName = null, location = null)

        assertEquals(stations, result)
        coVerify {
            radioStationRepository.getRadioStationsByCountry(offset = 0, limit = 10)
        }
    }

    @Test
    fun `when execute with search name then call searchRadioStations`() = runTest {
        val searchQuery = "jazz"
        val stations = listOf(newRadioStation())
        coEvery {
            radioStationRepository.searchRadioStations(searchQuery = searchQuery, offset = 0, limit = 10)
        } returns stations

        val result = useCase.execute(page = 1, searchName = searchQuery)

        assertEquals(stations, result)
        coVerify {
            radioStationRepository.searchRadioStations(searchQuery = searchQuery, offset = 0, limit = 10)
        }
    }
}
