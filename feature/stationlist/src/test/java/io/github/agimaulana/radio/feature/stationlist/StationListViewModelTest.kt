package io.github.agimaulana.radio.feature.stationlist

import app.cash.turbine.turbineScope
import io.github.agimaulana.radio.core.network.test.randomizer.randomString
import io.github.agimaulana.radio.core.network.test.randomizer.randomUrl
import io.github.agimaulana.radio.feature.stationlist.StationListViewModel.Action.LoadMore
import io.github.agimaulana.radio.feature.stationlist.datafactories.newRadioStation
import io.github.agimaulana.radio.feature.stationlist.datafactories.newUiStateStation
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StationListViewModelTest : StationListViewModelTest__Fixtures() {

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
                getRadioStationsUseCase.execute(page = 1, searchName = null)
            } returns stations
            val expectedStations = stations.map { it.toUiStateStation() }

            viewModel.init()

            with(uiState.awaitItem()) {
                assertEquals(1, currentPage)
                assertEquals(expectedStations, this.stations)
            }
            coVerify(exactly = 1) {
                getRadioStationsUseCase.execute(page = 1, searchName = null)
                radioPlayerControllerFactory.get()
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
                getRadioStationsUseCase.execute(page = 1, searchName = null)
            } returns stations1
            val expectedStations1 = stations1.map { it.toUiStateStation() }

            viewModel.init()

            with(uiState.awaitItem()) {
                assertEquals(1, currentPage)
                assertEquals(expectedStations1, this.stations)
            }
            coVerify(exactly = 1) {
                getRadioStationsUseCase.execute(page = 1, searchName = null)
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
                getRadioStationsUseCase.execute(page = 2, searchName = null)
            } returns stations2
            val expectedStations2 = stations2.map { it.toUiStateStation() }

            viewModel.onAction(LoadMore)

            with(uiState.awaitItem()) {
                assertEquals(2, currentPage)
                assertEquals(expectedStations1 + expectedStations2, this.stations)
            }
            coVerify(exactly = 1) {
                getRadioStationsUseCase.execute(page = 2, searchName = null)
            }
        }
    }

    @Test
    fun `given search by station name when load more then fetch next page of radio stations`() = runTest {
        turbineScope {
            val uiState = viewModel.uiState.testIn(backgroundScope)
            val searchName = randomString()

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
                getRadioStationsUseCase.execute(page = 1, searchName = searchName)
            } returns stations1
            val expectedStations1 = stations1.map { it.toUiStateStation() }

            viewModel.init()
            viewModel.onAction(StationListViewModel.Action.Search(searchName))

            with(uiState.expectMostRecentItem()) {
                assertEquals(1, currentPage)
                assertEquals(searchName, filterStationName)
                assertEquals(expectedStations1, this.stations)
            }
            coVerify(exactly = 1) {
                getRadioStationsUseCase.execute(page = 1, searchName = searchName)
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
                getRadioStationsUseCase.execute(page = 2, searchName = searchName)
            } returns stations2
            val expectedStations2 = stations2.map { it.toUiStateStation() }

            viewModel.onAction(LoadMore)

            with(uiState.awaitItem()) {
                assertEquals(2, currentPage)
                assertEquals(searchName, filterStationName)
                assertEquals(expectedStations1 + expectedStations2, this.stations)
            }
            coVerify(exactly = 1) {
                getRadioStationsUseCase.execute(page = 2, searchName = searchName)
            }
        }
    }

    @Test
    fun `given station is not playing when clicked then set radio and play`() {
        val station = newUiStateStation(
            withServerUuid = "radio-123",
            withName = "Radio 123",
        )
        every { radioPlayerController.currentMediaId } returns "radio-566"

        viewModel.init()
        viewModel.onAction(StationListViewModel.Action.Click(station))

        verify {
            radioPlayerController.setMediaItem(station.toRadioMediaItem())
            radioPlayerController.prepare()
            radioPlayerController.play()
        }
    }

    @Test
    fun `given station is the current radio media and playing when clicked then pause`() {
        val station = newUiStateStation(
            withServerUuid = "radio-123",
            withName = "Radio 123",
            withIsPlaying = true,
        )
        every { radioPlayerController.currentMediaId } returns station.serverUuid
        every { radioPlayerController.isPlaying } returns station.isPlaying

        viewModel.init()
        viewModel.onAction(StationListViewModel.Action.Click(station))

        verify {
            radioPlayerController.pause()
        }
    }

    @Test
    fun `given station is the current radio media and not playing when clicked then play`() {
        val station = newUiStateStation(
            withServerUuid = "radio-123",
            withName = "Radio 123",
            withIsPlaying = false,
        )
        every { radioPlayerController.currentMediaId } returns station.serverUuid
        every { radioPlayerController.isPlaying } returns station.isPlaying

        viewModel.init()
        viewModel.onAction(StationListViewModel.Action.Click(station))

        verify {
            radioPlayerController.play()
        }
    }

    @Test
    fun `when pause then pause radio player`() {
        val station = newUiStateStation(
            withServerUuid = "radio-123",
            withName = "Radio 123",
        )

        viewModel.init()
        viewModel.onAction(StationListViewModel.Action.Pause(station))

        verify {
            radioPlayerController.pause()
        }
    }

    @Test
    fun `when play then play radio player`() {
        val station = newUiStateStation(
            withServerUuid = "radio-123",
            withName = "Radio 123",
        )

        viewModel.init()
        viewModel.onAction(StationListViewModel.Action.Play(station))

        verify {
            radioPlayerController.play()
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
}
