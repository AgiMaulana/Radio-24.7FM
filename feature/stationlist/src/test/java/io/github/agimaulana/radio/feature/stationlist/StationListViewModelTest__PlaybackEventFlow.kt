package io.github.agimaulana.radio.feature.stationlist

import app.cash.turbine.turbineScope
import io.github.agimaulana.radio.core.radioplayer.PlaybackEvent
import io.github.agimaulana.radio.core.radioplayer.PlaybackState
import io.github.agimaulana.radio.feature.stationlist.datafactories.newRadioStation
import io.github.agimaulana.radio.feature.stationlist.datafactories.newUiStateStation
import io.mockk.coEvery
import io.mockk.every
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StationListViewModelTest__PlaybackEventFlow  : StationListViewModelTest__Fixtures() {

    @Test
    fun `when playback event flowed then consume`() = runTest {
        turbineScope {
            val station = newRadioStation(
                withStationUuid = "radio-123",
                withName = "Radio 123",
            )
            coEvery {
                radioBrowser.getStation(station.stationUuid)
            } returns station.toUiStateStation().toRadioMediaItem()
            val uiState = viewModel.uiState.testIn(backgroundScope)

            viewModel.init(hasLocationPermission = false, hasAskedPermission = false, shouldShowRationale = false)
            // region start: when media item transitioned then update selected station
            playbackEventChannel.send(PlaybackEvent.MediaItemTransition(station.stationUuid))

            with(uiState.expectMostRecentItem()) {
                assertEquals(station.toUiStateStation(), selectedStation)
            }
            // region end: when media item transitioned then update selected station

            // region start: when playback state changed then update selected station
            playbackEventChannel.send(PlaybackEvent.StateChanged(PlaybackState.BUFFERING))

            with(uiState.awaitItem()) {
                assertEquals(
                    station.toUiStateStation().copy(isBuffering = true),
                    selectedStation
                )
            }
            // region end: when playback state changed then update selected station

            // region start: when playing state changed then update selected station
            playbackEventChannel.send(PlaybackEvent.PlayingChanged(true))

            with(uiState.awaitItem()) {
                assertEquals(
                    station.toUiStateStation().copy(isPlaying = true, isBuffering = false),
                    selectedStation
                )
            }

            playbackEventChannel.send(PlaybackEvent.PlayingChanged(false))

            with(uiState.awaitItem()) {
                assertEquals(
                    station.toUiStateStation().copy(isPlaying = false, isBuffering = false),
                    selectedStation
                )
            }
            // region end: when playing state changed then update selected station
        }
    }

    @Test
    fun `when playlist changed then preserve station list shape`() = runTest {
        turbineScope {
            val uiStationOne = newUiStateStation(
                withServerUuid = "radio-1",
                withName = "Radio 1"
            )
            val uiStationTwo = newUiStateStation(
                withServerUuid = "radio-2",
                withName = "Radio 2"
            )
            coEvery {
                radioBrowser.getStations(
                    page = 0,
                    pageSize = 10,
                    searchName = null,
                    location = null
                )
            } returns listOf(
                uiStationOne.toRadioMediaItem(),
                uiStationTwo.toRadioMediaItem()
            )

            var currentMediaId = ""
            var isPlaying = false
            every { radioPlayerController.currentMediaId } answers { currentMediaId }
            every { radioPlayerController.isPlaying } answers { isPlaying }
            every { radioPlayerController.getPlaylist() } returns listOf(
                uiStationOne.toRadioMediaItem()
            )

            val uiState = viewModel.uiState.testIn(backgroundScope)

            viewModel.init(
                hasLocationPermission = false,
                hasAskedPermission = false,
                shouldShowRationale = false
            )
            viewModel.onAction(StationListViewModel.Action.OnLocationPermissionGranted(isGranted = false))
            runCurrent()

            with(uiState.expectMostRecentItem()) {
                assertEquals(2, stations.size)
                assertFalse(stations[0].isPlaying)
                assertFalse(stations[1].isPlaying)
            }

            currentMediaId = "radio-1"
            isPlaying = true
            playbackEventChannel.send(PlaybackEvent.PlaylistChanged)
            runCurrent()

            with(uiState.awaitItem()) {
                assertEquals(2, stations.size)
                assertEquals("radio-1", stations[0].serverUuid)
                assertTrue(stations[0].isPlaying)
                assertFalse(stations[1].isPlaying)
            }
        }
    }

}
