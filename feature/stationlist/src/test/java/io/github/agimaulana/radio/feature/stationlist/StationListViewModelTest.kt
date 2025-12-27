package io.github.agimaulana.radio.feature.stationlist

import androidx.lifecycle.ViewModel
import app.cash.turbine.turbineScope
import io.github.agimaulana.radio.core.network.test.CoroutineMainDispatcherRule
import io.github.agimaulana.radio.core.network.test.randomizer.randomString
import io.github.agimaulana.radio.core.network.test.randomizer.randomUrl
import io.github.agimaulana.radio.core.radioplayer.PlaybackEvent
import io.github.agimaulana.radio.core.radioplayer.RadioPlayerController
import io.github.agimaulana.radio.core.radioplayer.RadioPlayerControllerFactory
import io.github.agimaulana.radio.domain.api.entity.RadioStation
import io.github.agimaulana.radio.domain.api.usecase.GetRadioStationUseCase
import io.github.agimaulana.radio.domain.api.usecase.GetRadioStationsUseCase
import io.github.agimaulana.radio.feature.stationlist.StationListViewModel.Action.LoadMore
import io.github.agimaulana.radio.feature.stationlist.datafactories.newRadioStation
import io.github.agimaulana.radio.feature.stationlist.datafactories.newUiStateStation
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class StationListViewModelTest {

    @RelaxedMockK
    private lateinit var getRadioStationsUseCase: GetRadioStationsUseCase

    @RelaxedMockK
    private lateinit var radioPlayerControllerFactory: RadioPlayerControllerFactory

    @RelaxedMockK
    private lateinit var getRadioStationUseCase: GetRadioStationUseCase

    @RelaxedMockK
    private lateinit var radioPlayerController: RadioPlayerController

    private lateinit var playbackEventChannel: Channel<PlaybackEvent>

    private lateinit var viewModel: StationListViewModel

    @get:Rule
    val dispatcherRule = CoroutineMainDispatcherRule()

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        playbackEventChannel = Channel()
        every {
            radioPlayerController.event
        } returns playbackEventChannel.receiveAsFlow()
        coEvery {
            radioPlayerControllerFactory.get()
        } returns radioPlayerController
        viewModel = StationListViewModel(
            getRadioStationsUseCase = getRadioStationsUseCase,
            getRadioStationUseCase = getRadioStationUseCase,
            radioPlayerControllerFactory = radioPlayerControllerFactory
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
