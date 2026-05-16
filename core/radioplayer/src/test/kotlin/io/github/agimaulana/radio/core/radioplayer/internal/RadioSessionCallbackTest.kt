package io.github.agimaulana.radio.core.radioplayer.internal

import android.os.Bundle
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import io.github.agimaulana.radio.core.radioplayer.PlaybackExtras
import io.github.agimaulana.radio.domain.api.entity.RadioStation
import io.github.agimaulana.radio.domain.api.repository.CatalogState
import io.github.agimaulana.radio.domain.api.repository.CatalogStateRepository
import io.github.agimaulana.radio.domain.api.usecase.GetPinnedStationsUseCase
import io.github.agimaulana.radio.domain.api.usecase.GetRadioStationUseCase
import io.github.agimaulana.radio.domain.api.usecase.GetRadioStationsUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RadioSessionCallbackTest {

    private val player = mockk<Player>(relaxed = true)

    @Test
    fun onSetMediaItems_resolvesRadioStationAndExpandsPlaylist() = runTest {
        val station1 = radioStation(
            stationUuid = "station-1",
            name = "Station One",
            tags = listOf("news"),
            imageUrl = "https://example.com/one.png",
            url = "https://example.com/one",
            resolvedUrl = "https://stream.example.com/one"
        )
        val station2 = radioStation(
            stationUuid = "station-2",
            name = "Station Two",
            tags = listOf("talk"),
            imageUrl = "https://example.com/two.png",
            url = "https://example.com/two",
            resolvedUrl = "https://stream.example.com/two"
        )
        val callback = callbackForStations(listOf(station1, station2))
        val controller = controller()
        val session = session()

        val result = callback.onSetMediaItems(
            mediaSession = session,
            controller = controller,
            mediaItems = listOf(MediaItem.Builder().setMediaId("station-2").build()),
            startIndex = 0,
            startPositionMs = 1500L
        ).get()

        assertEquals(2, result.mediaItems.size)
        assertEquals("station-1", result.mediaItems[0].mediaId)
        assertEquals("station-2", result.mediaItems[1].mediaId)
        assertEquals("https://stream.example.com/two", result.mediaItems[1].localConfiguration?.uri.toString())
        assertEquals(1, result.startIndex)
        assertEquals(1500L, result.startPositionMs)
    }

    @Test
    fun onSetMediaItems_usesContextFromExtrasForSearchPlaylist() = runTest {
        val searchStation1 = radioStation(
            stationUuid = "search-1",
            name = "Jazz Station",
            tags = listOf("jazz"),
            imageUrl = "https://example.com/jazz1.png",
            url = "https://example.com/jazz1",
            resolvedUrl = "https://stream.example.com/jazz1"
        )
        val searchStation2 = radioStation(
            stationUuid = "search-2",
            name = "Jazz Station Two",
            tags = listOf("jazz"),
            imageUrl = "https://example.com/jazz2.png",
            url = "https://example.com/jazz2",
            resolvedUrl = "https://stream.example.com/jazz2"
        )
        val callback = callbackForSearchStations(listOf(searchStation1, searchStation2))
        val controller = controller()
        val session = session()

        val extras = Bundle().apply {
            putString(PlaybackExtras.KEY_CONTEXT_TYPE, "SEARCH")
            putString(PlaybackExtras.KEY_CONTEXT_QUERY, "jazz")
        }
        val mediaItem = MediaItem.Builder()
            .setMediaId("search-1")
            .setMediaMetadata(MediaMetadata.Builder().setExtras(extras).build())
            .build()

        val result = callback.onSetMediaItems(
            mediaSession = session,
            controller = controller,
            mediaItems = listOf(mediaItem),
            startIndex = 0,
            startPositionMs = 0L
        ).get()

        assertEquals(2, result.mediaItems.size)
        assertEquals("search-1", result.mediaItems[0].mediaId)
        assertEquals(0, result.startIndex)
    }

    @Test
    fun onPlaybackResumption_returnsFirstCatalogItemWhenNothingIsPlaying() = runTest {
        val station = radioStation(
            stationUuid = "station-3",
            name = "Station Three",
            tags = listOf("music"),
            imageUrl = "https://example.com/three.png",
            url = "https://example.com/three",
            resolvedUrl = "https://stream.example.com/three"
        )
        val callback = callbackForStations(listOf(station))
        val controller = controller()
        val session = session()

        val result = callback.onPlaybackResumption(
            mediaSession = session,
            controller = controller,
            isForPlayback = true
        ).get()

        assertNotNull(result.mediaItems)
        assertEquals(1, result.mediaItems.size)
        assertEquals("station-3", result.mediaItems[0].mediaId)
        assertEquals(0, result.startIndex)
        assertEquals(0L, result.startPositionMs)
    }

    @Test
    fun onGetChildren_returnsRootCategories() = runTest {
        val callback = callbackForStations(emptyList())
        val controller = controller()
        val session = librarySession()

        val result = callback.onGetChildren(
            session = session,
            browser = controller,
            parentId = RadioLibraryCatalog.ROOT_MEDIA_ID,
            page = 0,
            pageSize = 10,
            params = null
        ).get()

        val items = requireNotNull(result.value)
        assertEquals(2, items.size)
        assertEquals(RadioLibraryCatalog.PINNED_MEDIA_ID, items[0].mediaId)
        assertEquals(RadioLibraryCatalog.STATIONS_MEDIA_ID, items[1].mediaId)
    }

    @Test
    fun onGetItem_returnsResolvedStation() = runTest {
        val station = radioStation(
            stationUuid = "station-4",
            name = "Station Four",
            tags = listOf("music"),
            imageUrl = "https://example.com/four.png",
            url = "https://example.com/four",
            resolvedUrl = "https://stream.example.com/four"
        )
        val callback = callbackForStations(listOf(station))
        val controller = controller()
        val session = librarySession()

        val result = callback.onGetItem(
            session = session,
            browser = controller,
            mediaId = "station-4"
        ).get()

        val item = requireNotNull(result.value)
        assertEquals("station-4", item.mediaId)
        assertEquals("https://stream.example.com/four", item.localConfiguration?.uri.toString())
    }

    private fun callbackForStations(stations: List<RadioStation>): RadioSessionCallback {
        val useCase = mockk<GetRadioStationsUseCase>()
        val getPinnedStationsUseCase = mockk<GetPinnedStationsUseCase>()
        val getRadioStationUseCase = mockk<GetRadioStationUseCase>()
        val catalogStateRepository = mockk<CatalogStateRepository>(relaxed = true)
        coEvery { catalogStateRepository.load() } returns null
        every { getPinnedStationsUseCase.execute() } returns kotlinx.coroutines.flow.flowOf(emptyList())
        coEvery { useCase.execute(page = 1, searchName = null, location = null) } returns stations
        coEvery { useCase.execute(page = 2, searchName = null, location = null) } returns emptyList()
        coEvery { getRadioStationUseCase.execute(any()) } answers {
            val id = firstArg<String>()
            stations.first { it.stationUuid == id }
        }
        return RadioSessionCallback(
            RadioLibraryCatalog(
                useCase,
                getPinnedStationsUseCase,
                getRadioStationUseCase,
                catalogStateRepository
            )
        )
    }

    private fun callbackForSearchStations(stations: List<RadioStation>): RadioSessionCallback {
        val useCase = mockk<GetRadioStationsUseCase>()
        val getPinnedStationsUseCase = mockk<GetPinnedStationsUseCase>()
        val getRadioStationUseCase = mockk<GetRadioStationUseCase>()
        val catalogStateRepository = mockk<CatalogStateRepository>(relaxed = true)
        coEvery { catalogStateRepository.load() } returns CatalogState(source = CatalogState.Source.SEARCH, query = "jazz")
        every { getPinnedStationsUseCase.execute() } returns kotlinx.coroutines.flow.flowOf(emptyList())
        coEvery { useCase.execute(page = 1, searchName = "jazz", location = null) } returns stations
        coEvery { useCase.execute(page = 2, searchName = "jazz", location = null) } returns emptyList()
        coEvery { getRadioStationUseCase.execute(any()) } answers {
            val id = firstArg<String>()
            stations.first { it.stationUuid == id }
        }
        return RadioSessionCallback(
            RadioLibraryCatalog(
                useCase,
                getPinnedStationsUseCase,
                getRadioStationUseCase,
                catalogStateRepository
            )
        )
    }

    private fun controller(): MediaSession.ControllerInfo {
        return mockk<MediaSession.ControllerInfo>(relaxed = true).also {
            every { it.packageName } returns "test.controller"
        }
    }

    private fun session(): MediaSession {
        return mockk<MediaSession>(relaxed = true).also {
            every { it.player } returns player
            every { player.currentMediaItem } returns null
        }
    }

    private fun librarySession(): MediaLibraryService.MediaLibrarySession {
        return mockk<MediaLibraryService.MediaLibrarySession>(relaxed = true).also {
            every { it.player } returns player
        }
    }

    private fun radioStation(
        stationUuid: String,
        name: String,
        tags: List<String>,
        imageUrl: String,
        url: String,
        resolvedUrl: String,
    ): RadioStation {
        return RadioStation(
            stationUuid = stationUuid,
            name = name,
            tags = tags,
            imageUrl = imageUrl,
            url = url,
            resolvedUrl = resolvedUrl,
        )
    }
}
