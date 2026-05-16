package io.github.agimaulana.radio.core.radioplayer.internal

import io.github.agimaulana.radio.core.radioplayer.RadioPlayerController
import io.github.agimaulana.radio.domain.api.entity.RadioStation
import io.github.agimaulana.radio.domain.api.repository.CatalogState
import io.github.agimaulana.radio.domain.api.repository.CatalogStateRepository
import io.github.agimaulana.radio.domain.api.usecase.GetPinnedStationsUseCase
import io.github.agimaulana.radio.domain.api.usecase.GetRadioStationUseCase
import io.github.agimaulana.radio.domain.api.usecase.GetRadioStationsUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RadioLibraryCatalogTest {

    private val getRadioStationsUseCase = mockk<GetRadioStationsUseCase>()
    private val getPinnedStationsUseCase = mockk<GetPinnedStationsUseCase>()
    private val getRadioStationUseCase = mockk<GetRadioStationUseCase>()
    private val catalogStateRepository = mockk<CatalogStateRepository>(relaxed = true)

    @Test
    fun rootItem_returnsBrowsableRoot() {
        val catalog = RadioLibraryCatalog(
            getRadioStationsUseCase,
            getPinnedStationsUseCase,
            getRadioStationUseCase,
            catalogStateRepository
        )

        val root = catalog.rootItem()

        assertEquals(RadioLibraryCatalog.ROOT_MEDIA_ID, root.mediaId)
        assertTrue(root.mediaMetadata.isBrowsable == true)
        assertEquals("Radio 24.7FM", root.mediaMetadata.title?.toString())
    }

    @Test
    fun loadChildren_returns_requested_page_slice() = runTest {
        coEvery { catalogStateRepository.load() } returns null
        coEvery {
            getRadioStationsUseCase.execute(page = 1, searchName = null, location = null)
        } returns buildStations(1, 10)
        coEvery {
            getRadioStationsUseCase.execute(page = 2, searchName = null, location = null)
        } returns buildStations(11, 10)
        coEvery {
            getRadioStationsUseCase.execute(page = 3, searchName = null, location = null)
        } returns buildStations(21, 5)
        coEvery {
            getRadioStationsUseCase.execute(page = 4, searchName = null, location = null)
        } returns emptyList()

        val catalog = RadioLibraryCatalog(
            getRadioStationsUseCase,
            getPinnedStationsUseCase,
            getRadioStationUseCase,
            catalogStateRepository
        )

        val firstPage = catalog.loadChildren(page = 0, pageSize = 10)
        val secondPage = catalog.loadChildren(page = 1, pageSize = 10)
        val thirdPage = catalog.loadChildren(page = 2, pageSize = 10)
        val outOfRange = catalog.loadChildren(page = 3, pageSize = 10)

        assertEquals(10, firstPage.size)
        assertEquals("station-1", firstPage.first().mediaId)
        assertEquals("station-10", firstPage.last().mediaId)
        assertEquals(10, secondPage.size)
        assertEquals("station-11", secondPage.first().mediaId)
        assertEquals("station-20", secondPage.last().mediaId)
        assertEquals(5, thirdPage.size)
        assertEquals("station-21", thirdPage.first().mediaId)
        assertEquals("station-25", thirdPage.last().mediaId)
        assertTrue(outOfRange.isEmpty())
        coVerify(exactly = 4) { catalogStateRepository.save(match { it.page in 0..3 }) }
    }

    @Test
    fun loadChildren_omitsArtworkWhenImageUrlBlank() = runTest {
        coEvery { catalogStateRepository.load() } returns null
        coEvery {
            getRadioStationsUseCase.execute(page = 1, searchName = null, location = null)
        } returns listOf(
            RadioStation(
                stationUuid = "station-1",
                name = "Station 1",
                tags = listOf("Genre 1"),
                imageUrl = "",
                url = "https://example.com/stream-1",
                resolvedUrl = "https://example.com/resolved-1"
            )
        )
        coEvery {
            getRadioStationsUseCase.execute(page = 2, searchName = null, location = null)
        } returns emptyList()

        val catalog = RadioLibraryCatalog(
            getRadioStationsUseCase,
            getPinnedStationsUseCase,
            getRadioStationUseCase,
            catalogStateRepository
        )

        val children = catalog.loadChildren(page = 0, pageSize = 10)

        assertEquals(1, children.size)
        assertEquals(null, children[0].mediaMetadata.artworkUri)
    }

    @Test
    fun loadChildren_usesRestoredSearchState() = runTest {
        coEvery {
            catalogStateRepository.load()
        } returns CatalogState(
            query = "jazz",
            page = 2,
            source = CatalogState.Source.SEARCH,
        )
        coEvery {
            getRadioStationsUseCase.execute(page = 1, searchName = "jazz", location = null)
        } returns buildStations(1, 10)
        coEvery {
            getRadioStationsUseCase.execute(page = 2, searchName = "jazz", location = null)
        } returns emptyList()

        val catalog = RadioLibraryCatalog(
            getRadioStationsUseCase,
            getPinnedStationsUseCase,
            getRadioStationUseCase,
            catalogStateRepository
        )

        val children = catalog.loadChildren(page = 0, pageSize = 10)

        assertEquals(10, children.size)
        coVerify { catalogStateRepository.save(match { it.page == 0 }) }
    }

    @Test
    fun getPinned_returnsPinnedStations() = runTest {
        every { getPinnedStationsUseCase.execute() } returns kotlinx.coroutines.flow.flowOf(
            listOf(
                RadioStation(
                    stationUuid = "pinned-1",
                    name = "Pinned 1",
                    tags = listOf("Genre 1"),
                    imageUrl = "https://example.com/pinned-1.png",
                    url = "https://example.com/stream-pinned-1",
                    resolvedUrl = "https://example.com/resolved-pinned-1"
                )
            )
        )

        val catalog = RadioLibraryCatalog(
            getRadioStationsUseCase,
            getPinnedStationsUseCase,
            getRadioStationUseCase,
            catalogStateRepository
        )

        val pinned = catalog.getPinned()

        assertEquals(1, pinned.size)
        assertEquals("pinned-1", pinned.first().mediaId)
    }

    @Test
    fun getStations_usesExplicitSearchAndLocation() = runTest {
        val location = io.github.agimaulana.radio.domain.api.entity.GeoLatLong(-6.2, 106.8)
        coEvery { catalogStateRepository.load() } returns null
        coEvery {
            getRadioStationsUseCase.execute(page = 1, searchName = "jazz", location = location)
        } returns buildStations(1, 10)
        coEvery {
            getRadioStationsUseCase.execute(page = 2, searchName = "jazz", location = location)
        } returns emptyList()

        val catalog = RadioLibraryCatalog(
            getRadioStationsUseCase,
            getPinnedStationsUseCase,
            getRadioStationUseCase,
            catalogStateRepository
        )

        val stations = catalog.getStations(
            page = 0,
            pageSize = 10,
            search = "jazz",
            location = location
        )

        assertEquals(10, stations.size)
        coVerify {
            getRadioStationsUseCase.execute(page = 1, searchName = "jazz", location = location)
        }
        coVerify {
            catalogStateRepository.save(match {
                it.query == "jazz" &&
                    it.locationLat == location.latitude &&
                    it.locationLon == location.longitude &&
                    it.page == 0 &&
                    it.source == CatalogState.Source.LOCATION
            })
        }
    }

    @Test
    fun getPlaylistForContext_searchContext_loadsSearchPlaylist() = runTest {
        coEvery { catalogStateRepository.load() } returns null
        coEvery {
            getRadioStationsUseCase.execute(page = 1, searchName = "jazz", location = null)
        } returns buildStations(1, 5)
        coEvery {
            getRadioStationsUseCase.execute(page = 2, searchName = "jazz", location = null)
        } returns emptyList()

        val catalog = RadioLibraryCatalog(
            getRadioStationsUseCase,
            getPinnedStationsUseCase,
            getRadioStationUseCase,
            catalogStateRepository
        )

        val context = RadioPlayerController.PlaybackContext(
            type = RadioPlayerController.PlaybackContext.Type.SEARCH,
            query = "jazz"
        )
        val playlist = catalog.getPlaylistForContext(context)

        assertEquals(5, playlist.size)
        coVerify {
            catalogStateRepository.save(match {
                it.query == "jazz" && it.source == CatalogState.Source.SEARCH
            })
        }
    }

    @Test
    fun getPlaylistForContext_locationContext_loadsLocationPlaylist() = runTest {
        coEvery { catalogStateRepository.load() } returns null
        val location = io.github.agimaulana.radio.domain.api.entity.GeoLatLong(-6.2, 106.8)
        coEvery {
            getRadioStationsUseCase.execute(page = 1, searchName = null, location = location)
        } returns buildStations(1, 3)
        coEvery {
            getRadioStationsUseCase.execute(page = 2, searchName = null, location = location)
        } returns emptyList()

        val catalog = RadioLibraryCatalog(
            getRadioStationsUseCase,
            getPinnedStationsUseCase,
            getRadioStationUseCase,
            catalogStateRepository
        )

        val context = RadioPlayerController.PlaybackContext(
            type = RadioPlayerController.PlaybackContext.Type.LOCATION,
            location = RadioPlayerController.PlaybackContext.Location(-6.2, 106.8)
        )
        val playlist = catalog.getPlaylistForContext(context)

        assertEquals(3, playlist.size)
        coVerify {
            catalogStateRepository.save(match {
                it.locationLat == -6.2 && it.locationLon == 106.8 && it.source == CatalogState.Source.LOCATION
            })
        }
    }

    private fun buildStations(startIndex: Int, count: Int): List<RadioStation> {
        return (0 until count).map { offset ->
            val index = startIndex + offset
            RadioStation(
                stationUuid = "station-$index",
                name = "Station $index",
                tags = listOf("Genre $index"),
                imageUrl = "https://example.com/image-$index.png",
                url = "https://example.com/stream-$index",
                resolvedUrl = "https://example.com/resolved-$index"
            )
        }
    }
}
