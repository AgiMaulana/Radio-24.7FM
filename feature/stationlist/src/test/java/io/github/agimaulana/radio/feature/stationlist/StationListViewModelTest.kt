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
import io.github.agimaulana.radio.feature.stationlist.StationListViewModel.Companion.SEARCH_DEBOUNCE_MS
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

            // State updates immediately: keyword set, list cleared
            with(uiState.expectMostRecentItem()) {
                assertEquals(0, currentPage)
                assertEquals(searchName, filterStationName)
                assertTrue(stations.isEmpty())
            }

            // Advance past the debounce window so the fetch fires
            testScheduler.advanceTimeBy(SEARCH_DEBOUNCE_MS + 1)

            with(uiState.awaitItem()) {
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
    fun `given rapid typing when search then only one fetch is made after debounce`() = runTest {
        turbineScope {
            val uiState = viewModel.uiState.testIn(backgroundScope)
            uiState.awaitItem() // initial state

            val finalQuery = "radio"
            coEvery {
                getRadioStationsUseCase.execute(page = 1, searchName = finalQuery)
            } returns emptyList()

            // Simulate rapid keystrokes within the debounce window
            viewModel.onAction(StationListViewModel.Action.Search("r"))
            testScheduler.advanceTimeBy(100)
            viewModel.onAction(StationListViewModel.Action.Search("ra"))
            testScheduler.advanceTimeBy(100)
            viewModel.onAction(StationListViewModel.Action.Search("rad"))
            testScheduler.advanceTimeBy(100)
            viewModel.onAction(StationListViewModel.Action.Search("radi"))
            testScheduler.advanceTimeBy(100)
            viewModel.onAction(StationListViewModel.Action.Search(finalQuery))

            // Advance past the debounce window
            testScheduler.advanceTimeBy(SEARCH_DEBOUNCE_MS + 1)

            // Only the last query should have triggered a fetch
            coVerify(exactly = 1) {
                getRadioStationsUseCase.execute(page = 1, searchName = finalQuery)
            }
            coVerify(exactly = 0) {
                getRadioStationsUseCase.execute(page = 1, searchName = "r")
                getRadioStationsUseCase.execute(page = 1, searchName = "ra")
                getRadioStationsUseCase.execute(page = 1, searchName = "rad")
                getRadioStationsUseCase.execute(page = 1, searchName = "radi")
            }
            uiState.cancelAndConsumeRemainingEvents()
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

    @Test
    fun `given radio player has current media when init then restore selected station with playing state`() = runTest {
        turbineScope {
            val uiState = viewModel.uiState.testIn(backgroundScope)

            val currentMediaId = randomString(length = 24)
            every { radioPlayerController.currentMediaId } returns currentMediaId
            every { radioPlayerController.isPlaying } returns true

            val station = newRadioStation(
                withStationUuid = currentMediaId,
                withName = "Restored Radio",
                withTags = listOf("Pop"),
                withImageUrl = randomUrl(),
                withUrl = randomUrl(),
            )
            coEvery {
                getRadioStationUseCase.execute(currentMediaId)
            } returns station

            viewModel.init()

            // Wait for fetch then restore to complete
            uiState.awaitItem()
            uiState.awaitItem()

            with(uiState.expectMostRecentItem()) {
                assertEquals(currentMediaId, selectedStation?.serverUuid)
                assertEquals("Restored Radio", selectedStation?.name)
                assertEquals(true, selectedStation?.isPlaying)
            }
        }
    }

    @Test
    fun `given radio player has current media but not playing when init then restore selected station with not playing state`() = runTest {
        turbineScope {
            val uiState = viewModel.uiState.testIn(backgroundScope)

            val currentMediaId = randomString(length = 24)
            every { radioPlayerController.currentMediaId } returns currentMediaId
            every { radioPlayerController.isPlaying } returns false

            val station = newRadioStation(
                withStationUuid = currentMediaId,
                withName = "Restored Radio",
                withTags = listOf("Jazz"),
                withImageUrl = randomUrl(),
                withUrl = randomUrl(),
            )
            coEvery {
                getRadioStationUseCase.execute(currentMediaId)
            } returns station

            viewModel.init()

            // Wait for fetch then restore to complete
            uiState.awaitItem()
            uiState.awaitItem()

            with(uiState.expectMostRecentItem()) {
                assertEquals(currentMediaId, selectedStation?.serverUuid)
                assertEquals("Restored Radio", selectedStation?.name)
                assertEquals(false, selectedStation?.isPlaying)
            }
        }
    }

    @Test
    fun `given radio player has no current media when init then do not restore selected station`() = runTest {
        turbineScope {
            val uiState = viewModel.uiState.testIn(backgroundScope)

            every { radioPlayerController.currentMediaId } returns null

            viewModel.init()

            // Wait for fetch to complete
            uiState.awaitItem()

            with(uiState.expectMostRecentItem()) {
                assertEquals(null, selectedStation)
            }
            coVerify(exactly = 0) {
                getRadioStationUseCase.execute(any())
            }
        }
    }
}