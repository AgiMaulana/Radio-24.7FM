package io.github.agimaulana.radio.feature.auto.screen

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.ActionStrip
import androidx.car.app.model.ItemList
import androidx.car.app.model.ListTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.SectionedItemList
import androidx.car.app.model.Template
import io.github.agimaulana.radio.core.radioplayer.PlaybackEvent
import io.github.agimaulana.radio.core.radioplayer.RadioMediaItem
import io.github.agimaulana.radio.core.radioplayer.RadioPlayerController
import io.github.agimaulana.radio.domain.api.entity.RadioStation
import io.github.agimaulana.radio.domain.api.usecase.GetPinnedStationsUseCase
import io.github.agimaulana.radio.domain.api.usecase.GetRadioStationsUseCase
import io.github.agimaulana.radio.domain.api.usecase.PinStationUseCase
import io.github.agimaulana.radio.domain.api.usecase.UnpinStationUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BrowseScreen(
    carContext: CarContext,
    private val getRadioStationsUseCase: GetRadioStationsUseCase,
    private val getPinnedStationsUseCase: GetPinnedStationsUseCase,
    private val pinStationUseCase: PinStationUseCase,
    private val unpinStationUseCase: UnpinStationUseCase,
    private val playerController: StateFlow<RadioPlayerController?>,
) : Screen(carContext) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var pinnedStations: List<RadioStation> = emptyList()
    private var allStations: List<RadioStation> = emptyList()
    private var isLoading = true
    private var currentMediaId: String? = null
    private var isPlaying = false

    init {
        scope.launch {
            launch {
                getPinnedStationsUseCase.execute().collect { pinned ->
                    pinnedStations = pinned
                    invalidate()
                }
            }
            launch {
                runCatching {
                    allStations = getRadioStationsUseCase.execute(page = 0, searchName = null)
                }
                isLoading = false
                invalidate()
            }
            launch {
                playerController.filterNotNull().first().also { controller ->
                    currentMediaId = controller.currentMediaId
                    isPlaying = controller.isPlaying
                    invalidate()
                    controller.event.collect { event ->
                        when (event) {
                            is PlaybackEvent.PlayingChanged -> isPlaying = event.isPlaying
                            is PlaybackEvent.MediaItemTransition -> currentMediaId = event.mediaId
                            is PlaybackEvent.StateChanged -> Unit
                        }
                        invalidate()
                    }
                }
            }
        }
    }

    override fun onGetTemplate(): Template {
        if (isLoading) {
            return ListTemplate.Builder()
                .setLoading(true)
                .setHeaderAction(Action.APP_ICON)
                .build()
        }

        val pinnedItemList = ItemList.Builder().apply {
            if (pinnedStations.isEmpty()) {
                setNoItemsMessage("No pinned stations")
            }
            pinnedStations.take(6).forEach { station ->
                addItem(buildStationRow(station, RadioPlayerController.PlaybackContext.Type.PINNED))
            }
        }.build()

        val allItemList = ItemList.Builder().apply {
            if (allStations.isEmpty()) {
                setNoItemsMessage("No stations available")
            }
            allStations.take(6).forEach { station ->
                addItem(buildStationRow(station, RadioPlayerController.PlaybackContext.Type.DEFAULT))
            }
        }.build()

        return ListTemplate.Builder()
            .setHeaderAction(Action.APP_ICON)
            .addSectionedList(SectionedItemList.create(pinnedItemList, "PINNED"))
            .addSectionedList(SectionedItemList.create(allItemList, "ALL STATIONS"))
            .setActionStrip(buildActionStrip())
            .build()
    }

    private fun buildStationRow(
        station: RadioStation,
        contextType: RadioPlayerController.PlaybackContext.Type,
    ): Row {
        val isCurrentlyPlaying = currentMediaId == station.stationUuid && isPlaying
        return Row.Builder()
            .setTitle(station.name)
            .addText(station.tags.firstOrNull() ?: "")
            .addText(if (isCurrentlyPlaying) "▶ Now playing" else "")
            .setOnClickListener { onStationSelected(station, contextType) }
            .build()
    }

    private fun buildActionStrip(): ActionStrip {
        val actions = mutableListOf<Action>()

        if (currentMediaId != null) {
            actions += Action.Builder()
                .setTitle(if (isPlaying) "⏸" else "▶")
                .setOnClickListener {
                    if (isPlaying) playerController.value?.pause()
                    else playerController.value?.play()
                }
                .build()
        }

        actions += Action.Builder()
            .setTitle("Search")
            .setOnClickListener {
                screenManager.push(
                    SearchScreen(
                        carContext = carContext,
                        getRadioStationsUseCase = getRadioStationsUseCase,
                        getPinnedStationsUseCase = getPinnedStationsUseCase,
                        playerController = playerController,
                    )
                )
            }
            .build()

        return ActionStrip.Builder().apply {
            actions.forEach { addAction(it) }
        }.build()
    }

    private fun onStationSelected(
        station: RadioStation,
        contextType: RadioPlayerController.PlaybackContext.Type,
    ) {
        scope.launch {
            playerController.value?.startPlayback(
                items = listOf(station.toRadioMediaItem()),
                startIndex = 0,
                context = RadioPlayerController.PlaybackContext(type = contextType),
            )
        }
        screenManager.push(
            NowPlayingScreen(
                carContext = carContext,
                station = station,
                pinStationUseCase = pinStationUseCase,
                unpinStationUseCase = unpinStationUseCase,
                playerController = playerController,
            )
        )
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}

internal fun RadioStation.toRadioMediaItem(): RadioMediaItem = RadioMediaItem(
    mediaId = stationUuid,
    streamUrl = resolvedUrl.ifEmpty { url },
    radioMetadata = RadioMediaItem.RadioMetadata(
        stationName = name,
        genre = tags.firstOrNull() ?: "",
        imageUrl = imageUrl,
    )
)
