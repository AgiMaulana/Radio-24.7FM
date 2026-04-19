package io.github.agimaulana.radio.feature.stationlist

import app.cash.turbine.turbineScope
import io.github.agimaulana.radio.core.radioplayer.PlaybackEvent
import io.github.agimaulana.radio.core.radioplayer.PlaybackState
import io.github.agimaulana.radio.feature.stationlist.datafactories.newRadioStation
import io.mockk.coEvery
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
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
                getRadioStationUseCase.execute(station.stationUuid)
            } returns station
            val uiState = viewModel.uiState.testIn(backgroundScope)

            viewModel.init()
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

}
