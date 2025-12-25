package io.github.agimaulana.radio.feature.stationlist

import androidx.lifecycle.ViewModel
import app.cash.turbine.turbineScope
import io.github.agimaulana.radio.core.network.test.randomizer.randomString
import io.github.agimaulana.radio.core.network.test.randomizer.randomUrl
import io.github.agimaulana.radio.core.radioplayer.PlaybackEvent
import io.github.agimaulana.radio.core.radioplayer.PlaybackState
import io.github.agimaulana.radio.core.radioplayer.RadioMediaItem
import io.github.agimaulana.radio.core.radioplayer.RadioPlayerController
import io.github.agimaulana.radio.core.radioplayer.RadioPlayerControllerFactory
import io.github.agimaulana.radio.domain.api.entity.RadioStation
import io.github.agimaulana.radio.domain.api.usecase.GetRadioStationsUseCase
import io.github.agimaulana.radio.feature.stationlist.StationListViewModel.Action.LoadMore
import io.github.agimaulana.radio.feature.stationlist.datafactories.newRadioStation
import io.github.agimaulana.radio.feature.stationlist.datafactories.newUiStateStation
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class StationListViewModelTest {

    @RelaxedMockK
    private lateinit var getRadioStationsUseCase: GetRadioStationsUseCase

    @RelaxedMockK
    private lateinit var radioPlayerControllerFactory: RadioPlayerControllerFactory

    @RelaxedMockK
    private lateinit var radioPlayerController: RadioPlayerController

    private lateinit var playbackEventChannel: Channel<PlaybackEvent>

    private lateinit var viewModel: StationListViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        playbackEventChannel = Channel()
        every {
            radioPlayerController.event
        } returns playbackEventChannel.receiveAsFlow()
        every {
            radioPlayerControllerFactory.getAsync(any())
        } answers {
            firstArg<(RadioPlayerController) -> Unit>().invoke(radioPlayerController)
        }
        viewModel = StationListViewModel(
            getRadioStationsUseCase,
            radioPlayerControllerFactory
        )
    }

    @Test
    fun `when init then fetch first page of radio stations and create radio controller`() = runTest {
        turbineScope {
            val uiState = viewModel.uiState.testIn(backgroundScope)

            with(uiState.awaitItem()) {
                assertEquals(0, currentPage)
                assertTrue(stations.isEmpty())
            }

            // assert flow is being observed
            val stations = listOf(
                newRadioStation(
                    withStationUuid = randomString(length = 24),
                    withName = "Radio ${randomString(length = 5)}",
                    withTags = listOf(randomString()),
                    withImageUrl = randomUrl(),
                    withUrl = randomUrl(),
                )
            )
            coEvery {
                getRadioStationsUseCase.execute(page = 1)
            } returns stations
            val expectedStations = stations.map { it.toUiStateStation() }

            viewModel.init()

            with(uiState.awaitItem()) {
                assertEquals(1, currentPage)
                assertEquals(expectedStations, this.stations)
            }
            coVerify(exactly = 1) {
                getRadioStationsUseCase.execute(page = 1)
                radioPlayerControllerFactory.getAsync(any())
            }
        }
    }

    @Test
    fun `given radio stations has been fetched when load more then fetch next page of radio stations`() = runTest {
        turbineScope {
            val uiState = viewModel.uiState.testIn(backgroundScope)

            with(uiState.awaitItem()) {
                assertEquals(0, currentPage)
                assertTrue(stations.isEmpty())
            }

            val stations1 = listOf(
                newRadioStation(
                    withStationUuid = randomString(length = 24),
                    withName = "Radio 1 ${randomString(length = 5)}",
                    withTags = listOf(randomString()),
                    withImageUrl = randomUrl(),
                    withUrl = randomUrl(),
                )
            )
            coEvery {
                getRadioStationsUseCase.execute(page = 1)
            } returns stations1
            val expectedStations1 = stations1.map { it.toUiStateStation() }

            viewModel.init()

            with(uiState.awaitItem()) {
                assertEquals(1, currentPage)
                assertEquals(expectedStations1, this.stations)
            }
            coVerify(exactly = 1) {
                getRadioStationsUseCase.execute(page = 1)
            }

            val stations2 = listOf(
                newRadioStation(
                    withStationUuid = randomString(length = 24),
                    withName = "Radio 2 ${randomString(length = 5)}",
                    withTags = listOf(randomString()),
                    withImageUrl = randomUrl(),
                    withUrl = randomUrl(),
                )
            )
            coEvery {
                getRadioStationsUseCase.execute(page = 2)
            } returns stations2
            val expectedStations2 = stations2.map { it.toUiStateStation() }

            viewModel.onAction(LoadMore)

            with(uiState.awaitItem()) {
                assertEquals(2, currentPage)
                assertEquals(expectedStations1 + expectedStations2, this.stations)
            }
            coVerify(exactly = 1) {
                getRadioStationsUseCase.execute(page = 2)
            }
        }
    }

    @Test
    fun `given station is not selected when clicked then set as new media item and play`() = runTest {
        turbineScope {
            val station = newUiStateStation(
                withServerUuid = randomString(length = 24),
                withName = "Radio ${randomString(length = 5)}",
                withGenre = randomString(),
                withImageUrl = randomUrl(),
                withStreamUrl = randomUrl(),
                withIsBuffering = false,
                withIsPlaying = false,
            )
            val uiState = viewModel.uiState.testIn(backgroundScope)
            assertNull(uiState.awaitItem().selectedStation)

            // region start: assert not playing -> playing
            viewModel.init()
            viewModel.onAction(StationListViewModel.Action.Click(station))

            assertEquals(station, uiState.expectMostRecentItem().selectedStation)
            coVerifyOrder {
                radioPlayerController.setMediaItem(
                    RadioMediaItem(
                        mediaId = station.serverUuid,
                        streamUrl = station.streamUrl,
                        radioMetadata = RadioMediaItem.RadioMetadata(
                            stationName = station.name,
                            genre = station.genre,
                            imageUrl = station.imageUrl,
                        )
                    )
                )
                radioPlayerController.prepare()
                radioPlayerController.play()
            }

            playbackEventChannel.send(PlaybackEvent.StateChanged(PlaybackState.BUFFERING))

            with(uiState.awaitItem()) {
                assertTrue(selectedStation!!.isBuffering)
                assertFalse(selectedStation!!.isPlaying)
            }

            playbackEventChannel.send(PlaybackEvent.StateChanged(PlaybackState.PLAYING))

            with(uiState.awaitItem()) {
                assertFalse(selectedStation!!.isBuffering)
                assertTrue(selectedStation!!.isPlaying)
            }
            // region end: assert not playing -> playing

            // region start: assert playing -> pause
            viewModel.onAction(StationListViewModel.Action.Click(station))

            verify {
                radioPlayerController.pause()
            }
            // region end: assert playing -> pause

            // region start: assert paused -> play
            viewModel.onAction(StationListViewModel.Action.Click(station))

            verify {
                radioPlayerController.play()
            }
            // region end: assert paused -> play
        }

        @Test
        fun `given station is playing when pause then pause`() = runTest {
            turbineScope {
                val station = newUiStateStation(
                    withServerUuid = randomString(length = 24),
                    withName = "Radio ${randomString(length = 5)}",
                    withGenre = randomString(),
                    withImageUrl = randomUrl(),
                    withStreamUrl = randomUrl(),
                    withIsBuffering = false,
                    withIsPlaying = true,
                )
                val uiState = viewModel.uiState.testIn(backgroundScope)

                viewModel.onAction(StationListViewModel.Action.Click(station))
                playbackEventChannel.send(PlaybackEvent.StateChanged(PlaybackState.PLAYING))

                assertTrue(uiState.expectMostRecentItem().selectedStation!!.isPlaying)

                // region start: assert playing -> pause
                viewModel.onAction(StationListViewModel.Action.Pause(station))

                verify {
                    radioPlayerController.pause()
                }
                // region end: assert playing -> pause

                // region start: assert paused -> play
                viewModel.onAction(StationListViewModel.Action.Play(station))

                verify {
                    radioPlayerController.play()
                }
                // region end: assert paused -> play

                // region start: assert selected then stop -> null
                viewModel.onAction(StationListViewModel.Action.Stop(station))

                verify {
                    radioPlayerController.stop()
                }
                assertNull(uiState.awaitItem().selectedStation)
                // region end: assert selected then stop -> null
            }
        }
    }

    @Test
    fun `when on clear view model then release radio player`() = runTest {
        viewModel.init()
        viewModel.invokeOnCleared()

        verify {
            radioPlayerController.release()
        }
    }

    private fun ViewModel.invokeOnCleared() {
        val onCleared = this::class.java.getDeclaredMethod("onCleared")
        onCleared.isAccessible = true
        onCleared.invoke(this)
    }

    private fun RadioStation.toUiStateStation() = newUiStateStation(
        withServerUuid = stationUuid,
        withName = name,
        withGenre = tags.getOrNull(0).orEmpty(),
        withImageUrl = imageUrl,
        withStreamUrl = url,
        withIsBuffering = false,
        withIsPlaying = false,
    )
}
