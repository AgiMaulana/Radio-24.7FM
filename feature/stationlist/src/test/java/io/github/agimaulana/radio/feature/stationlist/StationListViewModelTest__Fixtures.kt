package io.github.agimaulana.radio.feature.stationlist

import androidx.lifecycle.ViewModel
import app.cash.turbine.turbineScope
import io.github.agimaulana.radio.core.network.test.CoroutineMainDispatcherRule
import io.github.agimaulana.radio.core.network.test.randomizer.randomString
import io.github.agimaulana.radio.core.network.test.randomizer.randomUrl
import io.github.agimaulana.radio.core.radioplayer.PlaybackEvent
import io.github.agimaulana.radio.core.radioplayer.RadioMediaItem
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

abstract class StationListViewModelTest__Fixtures {

    @RelaxedMockK
    protected lateinit var getRadioStationsUseCase: GetRadioStationsUseCase

    @RelaxedMockK
    protected lateinit var radioPlayerControllerFactory: RadioPlayerControllerFactory

    @RelaxedMockK
    protected lateinit var getRadioStationUseCase: GetRadioStationUseCase

    @RelaxedMockK
    protected lateinit var radioPlayerController: RadioPlayerController

    protected lateinit var playbackEventChannel: Channel<PlaybackEvent>

    protected lateinit var viewModel: StationListViewModel

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
    fun runAllExtender() = Unit

    protected fun ViewModel.invokeOnCleared() {
        val onCleared = this::class.java.getDeclaredMethod("onCleared")
        onCleared.isAccessible = true
        onCleared.invoke(this)
    }

    protected fun RadioStation.toUiStateStation() = newUiStateStation(
        withServerUuid = stationUuid,
        withName = name,
        withGenre = tags.getOrNull(0).orEmpty(),
        withImageUrl = imageUrl,
        withStreamUrl = url,
        withIsBuffering = false,
        withIsPlaying = false,
    )

    protected fun StationListViewModel.UiState.Station.toRadioMediaItem(): RadioMediaItem {
        return RadioMediaItem(
            mediaId = serverUuid,
            streamUrl = streamUrl,
            radioMetadata = RadioMediaItem.RadioMetadata(
                stationName = name,
                genre = genre,
                imageUrl = imageUrl,
            )
        )
    }
}
