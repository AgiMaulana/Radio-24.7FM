package io.github.agimaulana.radio.infrastructure.repository

import io.github.agimaulana.radio.core.network.test.TestApiRule
import io.github.agimaulana.radio.core.network.test.resource.loadJsonResource
import io.github.agimaulana.radio.core.network.test.TestDispatcherProvider
import io.github.agimaulana.radio.domain.api.entity.GeoLatLong
import io.github.agimaulana.radio.domain.api.repository.RadioStationRepository
import io.github.agimaulana.radio.infrastructure.api.RadioStationApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class RadioStationRepositoryImplTest {

    @get:Rule
    val testApiRule = TestApiRule()

    private lateinit var repository: RadioStationRepository

    @Before
    fun setup() {
        repository = RadioStationRepositoryImpl(
            radioStationApi = testApiRule.create(RadioStationApi::class),
            dispatcherProvider = TestDispatcherProvider()
        )
    }

    @Test
    fun `when getRadioStations with location success then request correct URL with lat long and distance`() = runTest {
        val data = loadJsonResource("get_radio_stations_success_response.json")
        testApiRule.setResponse(200, data)
        val location = GeoLatLong(-6.2, 106.8)
        val distance = 50000
        val offset = 0
        val limit = 10

        repository.getRadioStations(location = location, distance = distance, offset = offset, limit = limit)

        with(testApiRule.takeLastRequest()) {
            val path = pathWithQueryParams ?: ""
            assertEquals("GET", method)
            assertTrue(path.contains("/json/stations/search"))
            assertTrue(path.contains("lat=${location.latitude}"))
            assertTrue(path.contains("long=${location.longitude}"))
            assertTrue(path.contains("distance=$distance"))
            assertTrue(path.contains("order=distance"))
            assertTrue(path.contains("reverse=false"))
        }
    }

    @Test
    fun `when getRadioStationsByCountry success then request correct URL`() = runTest {
        val data = loadJsonResource("get_radio_stations_success_response.json")
        testApiRule.setResponse(200, data)
        val offset = 0
        val limit = 10

        repository.getRadioStationsByCountry(offset = offset, limit = limit)

        with(testApiRule.takeLastRequest()) {
            val path = pathWithQueryParams ?: ""
            assertEquals("GET", method)
            assertTrue(path.contains("/json/stations/bycountry/indonesia"))
            assertTrue(path.contains("offset=$offset"))
            assertTrue(path.contains("limit=$limit"))
        }
    }

    @Test
    fun `when searchRadioStations then hit search endpoint with correct query params`() = runTest {
        val data = loadJsonResource("get_radio_stations_success_response.json")
        testApiRule.setResponse(200, data)
        val query = "Mosh"
        val offset = 0
        val limit = 10

        repository.searchRadioStations(query, offset, limit)

        with(testApiRule.takeLastRequest()) {
            val path = pathWithQueryParams ?: ""
            assertEquals("GET", method)
            assertTrue(path.contains("/json/stations/search"))
            assertTrue(path.contains("name=$query"))
        }
    }
}
