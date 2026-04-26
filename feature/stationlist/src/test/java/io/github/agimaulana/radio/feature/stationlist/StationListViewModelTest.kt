package io.github.agimaulana.radio.feature.stationlist

import app.cash.turbine.turbineScope
import io.github.agimaulana.radio.domain.api.entity.GeoLatLong
import io.github.agimaulana.radio.feature.stationlist.datafactories.newUiStateStation
import io.github.agimaulana.radio.feature.stationlist.location.LocationProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StationListViewModelTest : StationListViewModelTest__Fixtures() {

    @Test
    fun `initial state should have isLoading true`() = runTest {
        assertTrue(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `when init then create radio controller`() = runTest {
        turbineScope {
            val uiState = viewModel.uiState.testIn(backgroundScope)
            uiState.awaitItem() // initial

            viewModel.init()

            coVerify(exactly = 1) {
                radioPlayerControllerFactory.get()
            }
        }
    }

    @Test
    fun `given location permission granted when OnLocationPermissionGranted then fetch stations with location`() = runTest {
        turbineScope {
            val uiState = viewModel.uiState.testIn(backgroundScope)
            uiState.awaitItem() // initial

            val locationInfo = LocationProvider.LocationInfo(
                city = "Jakarta",
                country = "Indonesia",
                latitude = -6.2,
                longitude = 106.8
            )
            coEvery { locationProvider.getCurrentLocation() } returns locationInfo
            coEvery {
                getRadioStationsUseCase.execute(page = 1, searchName = null, location = any())
            } returns emptyList()

            viewModel.onAction(StationListViewModel.Action.OnLocationPermissionGranted(isGranted = true))

            // Sheet hidden immediately
            with(uiState.awaitItem()) {
                assertFalse(showLocationPermissionSheet)
            }

            with(uiState.awaitItem()) {
                assertEquals("Jakarta, Indonesia", locationName)
                assertEquals(GeoLatLong(-6.2, 106.8), currentPosition)
            }

            // Third emission: fetch complete
            with(uiState.awaitItem()) {
                assertEquals(1, currentPage)
                assertFalse(isLoading)
            }

            coVerify {
                getRadioStationsUseCase.execute(
                    page = 1,
                    searchName = null,
                    location = GeoLatLong(-6.2, 106.8)
                )
            }
        }
    }

    @Test
    fun `given location permission denied when OnLocationPermissionGranted then fetch without location`() = runTest {
        turbineScope {
            val uiState = viewModel.uiState.testIn(backgroundScope)
            uiState.awaitItem() // initial

            coEvery {
                getRadioStationsUseCase.execute(page = 1, searchName = null, location = null)
            } returns emptyList()

            viewModel.onAction(StationListViewModel.Action.OnLocationPermissionGranted(isGranted = false))

            // First emission: sheet hidden
            with(uiState.awaitItem()) {
                assertFalse(showLocationPermissionSheet)
            }

            // Second emission: fetch complete
            with(uiState.awaitItem()) {
                assertEquals(1, currentPage)
                assertFalse(isLoading)
            }

            coVerify {
                getRadioStationsUseCase.execute(page = 1, searchName = null, location = null)
            }
        }
    }

    @Test
    fun `given station is not playing when clicked then set radio and play`() {
        val station = newUiStateStation(withServerUuid = "radio-1")
        every { radioPlayerController.currentMediaId } returns "radio-2"

        viewModel.init()
        viewModel.onAction(StationListViewModel.Action.Click(station))

        verify {
            radioPlayerController.setMediaItem(any())
            radioPlayerController.prepare()
            radioPlayerController.play()
        }
    }

    @Test
    fun `when pause then pause radio player`() {
        val station = newUiStateStation()
        viewModel.init()
        viewModel.onAction(StationListViewModel.Action.Pause(station))
        verify { radioPlayerController.pause() }
    }

    @Test
    fun `when play then play radio player`() {
        val station = newUiStateStation()
        viewModel.init()
        viewModel.onAction(StationListViewModel.Action.Play(station))
        verify { radioPlayerController.play() }
    }

    @Test
    fun `when on clear view model then release radio player`() = runTest {
        viewModel.init()
        viewModel.invokeOnCleared()
        verify { radioPlayerController.release() }
    }
}
