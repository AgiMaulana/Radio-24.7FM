package io.github.agimaulana.radio.feature.stationlist

import android.content.Context
import androidx.lifecycle.ViewModel
import io.github.agimaulana.radio.core.network.test.CoroutineMainDispatcherRule
import io.github.agimaulana.radio.core.radioplayer.PlaybackEvent
import io.github.agimaulana.radio.core.radioplayer.RadioBrowserController
import io.github.agimaulana.radio.core.radioplayer.RadioBrowserFactory
import io.github.agimaulana.radio.core.radioplayer.RadioMediaItem
import io.github.agimaulana.radio.core.radioplayer.RadioPlayerController
import io.github.agimaulana.radio.core.radioplayer.RadioPlayerControllerFactory
import io.github.agimaulana.radio.domain.api.entity.RadioStation
import io.github.agimaulana.radio.domain.api.usecase.PinStationUseCase
import io.github.agimaulana.radio.domain.api.usecase.UnpinStationUseCase
import io.github.agimaulana.radio.feature.stationlist.datafactories.newUiStateStation
import io.github.agimaulana.radio.feature.stationlist.location.LocationProvider
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.receiveAsFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test

abstract class StationListViewModelTest__Fixtures {

    @RelaxedMockK
    protected lateinit var radioPlayerControllerFactory: RadioPlayerControllerFactory

    @RelaxedMockK
    protected lateinit var radioBrowserFactory: RadioBrowserFactory

    @RelaxedMockK
    protected lateinit var pinStationUseCase: PinStationUseCase

    @RelaxedMockK
    protected lateinit var unpinStationUseCase: UnpinStationUseCase

    @RelaxedMockK
    protected lateinit var radioPlayerController: RadioPlayerController

    @RelaxedMockK
    protected lateinit var stationListTracker: StationListTracker

    @RelaxedMockK
    protected lateinit var locationProvider: LocationProvider

    @MockK
    protected lateinit var context: Context

    protected lateinit var playbackEventChannel: Channel<PlaybackEvent>
    protected lateinit var radioBrowser: RadioBrowserController
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
        every {
            radioPlayerController.currentMediaId
        } returns ""
        every {
            radioPlayerController.isPlaying
        } returns false
        coEvery {
            radioPlayerControllerFactory.get()
        } returns radioPlayerController
        radioBrowser = mockk(relaxed = true)
        every {
            radioBrowser.pinnedStations
        } returns flowOf(emptyList())
        coEvery {
            radioBrowser.getStation(any())
        } returns null
        coEvery {
            radioBrowserFactory.get()
        } returns radioBrowser
        coEvery { radioBrowser.getStations(any(), any(), any(), any()) } returns emptyList<RadioMediaItem>()

        viewModel = StationListViewModel(
            pinStationUseCase = pinStationUseCase,
            unpinStationUseCase = unpinStationUseCase,
            radioPlayerControllerFactory = radioPlayerControllerFactory,
            radioBrowserFactory = radioBrowserFactory,
            stationListTracker = stationListTracker,
            locationProvider = locationProvider,
            context = context
        )
    }

    @Test
    fun runAllExtender() = Unit

    protected fun ViewModel.invokeOnCleared() {
        val onCleared = this::class.java.getDeclaredMethod("onCleared")
        onCleared.isAccessible = true
        onCleared.invoke(this)
    }

    protected fun RadioStation.toUiStateStation(): StationListViewModel.UiState.Station {
        val isCurrentlyPlaying = radioPlayerController.currentMediaId == stationUuid
        return newUiStateStation(
            withServerUuid = stationUuid,
            withName = name,
            withGenre = tags.getOrNull(0).orEmpty(),
            withImageUrl = imageUrl,
            withStreamUrl = url,
            withIsBuffering = false,
            withIsPlaying = isCurrentlyPlaying && radioPlayerController.isPlaying,
        )
    }

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

    protected fun StationListViewModel.UiState.Station.toRadioStation(): RadioStation {
        return RadioStation(
            stationUuid = serverUuid,
            name = name,
            tags = listOf(genre).filter { it.isNotBlank() },
            imageUrl = imageUrl,
            url = streamUrl,
            resolvedUrl = streamUrl,
        )
    }
}
