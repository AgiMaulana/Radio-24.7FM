package io.github.agimaulana.radio.core.radioplayer.internal

import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import io.github.agimaulana.radio.domain.api.entity.RadioStation
import io.github.agimaulana.radio.domain.api.repository.CatalogStateRepository
import io.github.agimaulana.radio.domain.api.usecase.GetRadioStationsUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verifyOrder
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

    private fun callbackForStations(stations: List<RadioStation>): RadioSessionCallback {
        val useCase = mockk<GetRadioStationsUseCase>()
        val catalogStateRepository = mockk<CatalogStateRepository>(relaxed = true)
        coEvery { catalogStateRepository.load() } returns null
        coEvery { useCase.execute(page = 1, searchName = null, location = null) } returns stations
        coEvery { useCase.execute(page = 2, searchName = null, location = null) } returns emptyList()
        return RadioSessionCallback(RadioLibraryCatalog(useCase, catalogStateRepository))
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
