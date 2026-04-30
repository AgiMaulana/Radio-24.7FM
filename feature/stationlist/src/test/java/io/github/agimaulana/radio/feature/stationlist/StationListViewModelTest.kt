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
import kotlinx.coroutines.test.runCurrent
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
            runCurrent()

            coVerify(exactly = 1) {
                radioPlayerControllerFactory.get()
            }
        }
    }

    @Test
    fun `given permission granted when init then do not show permission sheet`() = runTest {
        turbineScope {
            val uiState = viewModel.uiState.testIn(backgroundScope)
            uiState.awaitItem() // initial

            viewModel.init(hasLocationPermission = true)
            runCurrent()

            // Sheet should remain false (already granted)
            assertFalse(uiState.awaitItem().showLocationPermissionSheet)
        }
    }

    @Test
    fun `given no permission and shouldShowRationale false when init then do not show permission sheet`() = runTest {
        turbineScope {
            val uiState = viewModel.uiState.testIn(backgroundScope)
            uiState.awaitItem() // initial

            // Permanently denied: hasAskedPermission=true + shouldShowRationale=false
            viewModel.init(hasLocationPermission = false, hasAskedPermission = true, shouldShowRationale = false)
            runCurrent()

            // Sheet stays false (permanently denied)
            assertFalse(uiState.awaitItem().showLocationPermissionSheet)
        }
    }

    @Test
    fun `given no permission and shouldShowRationale true when init then show permission sheet`() = runTest {
        viewModel.init(hasLocationPermission = false, hasAskedPermission = true, shouldShowRationale = true)
        runCurrent()

        // Sheet should show (previously denied, can ask again)
        assertTrue(viewModel.uiState.value.showLocationPermissionSheet)
    }

    @Test
    fun `given hasAskedPermission and shouldShowRationale false when init then hide sheet`() = runTest {
        viewModel.init(hasLocationPermission = false, hasAskedPermission = true, shouldShowRationale = false)
        runCurrent()

        // Permanently denied - hide sheet
        assertFalse(viewModel.uiState.value.showLocationPermissionSheet)
    }

    @Test
    fun `given hasLocationPermission when init then hide sheet`() = runTest {
        viewModel.init(hasLocationPermission = true, hasAskedPermission = true, shouldShowRationale = false)
        runCurrent()

        assertFalse(viewModel.uiState.value.showLocationPermissionSheet)
    }

    @Test
    fun `given never asked and no permission when init then show sheet`() = runTest {
        viewModel.init(hasLocationPermission = false, hasAskedPermission = false, shouldShowRationale = false)
        runCurrent()

        assertTrue(viewModel.uiState.value.showLocationPermissionSheet)
    }

    @Test
    fun `given location permission granted when OnLocationPermissionGranted then fetch stations with location`() = runTest {
        turbineScope {
            val uiState = viewModel.uiState.testIn(backgroundScope)
            uiState.awaitItem() // initial

            val locationInfo = LocationProvider.LocationInfo(
                city = "Indonesia",
                adminArea = "Jakarta",
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

        viewModel.init(hasLocationPermission = false, shouldShowRationale = false)
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
        viewModel.init(hasLocationPermission = false, shouldShowRationale = false)
        viewModel.onAction(StationListViewModel.Action.Pause(station))
        verify { radioPlayerController.pause() }
    }

    @Test
    fun `when play then play radio player`() {
        val station = newUiStateStation()
        viewModel.init(hasLocationPermission = false, shouldShowRationale = false)
        viewModel.onAction(StationListViewModel.Action.Play(station))
        verify { radioPlayerController.play() }
    }

    @Test
    fun `when pin station then execute use case`() = runTest {
        val station = newUiStateStation(withServerUuid = "uuid-1")
        val domainStation = newRadioStation(withStationUuid = "uuid-1")
        coEvery { getRadioStationUseCase.execute("uuid-1") } returns domainStation

        viewModel.onAction(StationListViewModel.Action.PinStation(station))
        runCurrent()

        coVerify { pinStationUseCase.execute(domainStation) }
    }

    @Test
    fun `when unpin station then execute use case`() = runTest {
        viewModel.onAction(StationListViewModel.Action.UnpinStation("uuid-1"))
        runCurrent()

        coVerify { unpinStationUseCase.execute("uuid-1") }
    }

    @Test
    fun `when on clear view model then release radio player`() = runTest {
        viewModel.init(hasLocationPermission = false, shouldShowRationale = false)
        viewModel.invokeOnCleared()
        verify { radioPlayerController.release() }
    }
}
