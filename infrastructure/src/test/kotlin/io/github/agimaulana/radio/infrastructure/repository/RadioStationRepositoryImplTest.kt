package io.github.agimaulana.radio.infrastructure.repository

import io.github.agimaulana.radio.core.network.test.TestApiRule
import io.github.agimaulana.radio.core.network.test.resource.loadJsonResource
import io.github.agimaulana.radio.core.network.test.TestDispatcherProvider
import io.github.agimaulana.radio.domain.api.repository.RadioStationRepository
import io.github.agimaulana.radio.infrastructure.api.RadioStationApi
import io.github.agimaulana.radio.infrastructure.datafactories.newRadioStation
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
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
    fun `when getRadioStations success then parse json and return radio stations with correct offset and limit`() = runTest {
        val data = loadJsonResource("get_radio_stations_success_response.json")
        testApiRule.setResponse(200, data)
        val offset = 0
        val limit = 10

        val radioStations = repository.getRadioStations(offset, limit)

        assertEquals(createExpectedRadioStations(), radioStations)
        with(testApiRule.takeLastRequest()) {
            assertEquals("GET", method)
            assertEquals("/json/stations/bycountry/indonesia?offset=$offset&limit=$limit", pathWithQueryParams)
        }
    }

    @Test
    fun `when getRadioStations with offset 10 then request correct offset`() = runTest {
        val data = loadJsonResource("get_radio_stations_success_response.json")
        testApiRule.setResponse(200, data)
        val offset = 10
        val limit = 20

        repository.getRadioStations(offset, limit)

        with(testApiRule.takeLastRequest()) {
            assertEquals("/json/stations/bycountry/indonesia?offset=$offset&limit=$limit", pathWithQueryParams)
        }
    }

    private fun createExpectedRadioStations() = listOf(
        newRadioStation(
            withStationUuid = "6bd67acb-e99e-4673-8270-19d55935be2a",
            withName = "Mosh Head Black Metal ID",
            withTags = listOf(
                "black metal",
                "death metal",
                "heavy metal"
            ),
            withImageUrl = "https://i.imgur.com/ldM8wDn.jpeg",
            withUrl = "https://moshhead-blackmetal.stream.laut.fm/moshhead-blackmetal",
            withResolvedUrl = "https://moshhead-blackmetal.stream.laut.fm/moshhead-blackmetal",
        ),
        newRadioStation(
            withStationUuid = "16cc7691-8785-4e36-ae75-c9376eedfdcf",
            withName = "Most 105.8 FM Jakarta",
            withTags = listOf(
                "90s",
                "culture",
                "greatest hits",
                "the best of 80's"
            ),
            withImageUrl = "https://images.noiceid.cc/catalog/content-1614529804201.jpg",
            withUrl = "https://wz.mari.co.id:1936/web_mostfm/mostfm/chunklist_w405836415.m3u8",
            withResolvedUrl = "https://wz.mari.co.id:1936/web_mostfm/mostfm/chunklist_w405836415.m3u8",
        )
    )
}
