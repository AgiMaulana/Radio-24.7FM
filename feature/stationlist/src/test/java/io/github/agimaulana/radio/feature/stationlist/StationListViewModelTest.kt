package io.github.agimaulana.radio.feature.stationlist

import app.cash.turbine.turbineScope
import io.github.agimaulana.radio.domain.api.entity.GeoLatLong
import io.github.agimaulana.radio.feature.stationlist.datafactories.newRadioStation
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
    fun `when init then fetch first page of radio stations and create radio controller`() = runTest {
        turbineScope {
            val uiState = viewModel.uiState.testIn(backgroundScope)
            uiState.awaitItem() // initial

            val stations = listOf(newRadioStation())
            coEvery {
                getRadioStationsUseCase.execute(page = 1, searchName = null, location = null)
            } returns stations

            viewModel.init()

            with(uiState.awaitItem()) {
                assertEquals(1, currentPage)
                assertEquals(stations.size, this.stations.size)
            }
            coVerify(exactly = 1) {
                getRadioStationsUseCase.execute(page = 1, searchName = null, location = null)
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

            viewModel.onAction(StationListViewModel.Action.OnLocationPermissionGranted)

            // Sheet hidden immediately
            with(uiState.awaitItem()) {
                assertFalse(showLocationPermissionSheet)
            }

            with(uiState.awaitItem()) {
                assertEquals("Jakarta, Indonesia", locationName)
                assertEquals(GeoLatLong(-6.2, 106.8), currentPosition)
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
    fun `when DismissLocationPermission then hide permission sheet`() = runTest {
        turbineScope {
            val uiState = viewModel.uiState.testIn(backgroundScope)
            assertTrue(uiState.awaitItem().showLocationPermissionSheet)

            viewModel.onAction(StationListViewModel.Action.DismissLocationPermission)

            assertFalse(uiState.awaitItem().showLocationPermissionSheet)
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
