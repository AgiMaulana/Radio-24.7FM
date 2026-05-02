package io.github.agimaulana.radio.feature.auto.screen

import android.util.Log
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.ActionStrip
import androidx.car.app.model.CarIcon
import androidx.car.app.model.GridItem
import androidx.car.app.model.GridTemplate
import androidx.car.app.model.ItemList
import androidx.car.app.model.ListTemplate
import androidx.car.app.model.MessageTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.SearchTemplate
import androidx.car.app.model.SectionedItemList
import androidx.car.app.model.Tab
import androidx.car.app.model.TabContents
import androidx.car.app.model.TabTemplate
import androidx.car.app.model.Template
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
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
import timber.log.Timber

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
    private var activeTab = TAB_BROWSE
    private var searchQuery = ""
    private var searchResults: List<RadioStation> = emptyList()

    companion object {
        const val TAB_BROWSE = "browse"
        const val TAB_PINNED = "pinned"
        const val TAB_SEARCH = "search"
    }

    init {
        scope.launch {
            launch {
                getPinnedStationsUseCase.execute().collect { pinned ->
                    pinnedStations = pinned
                    invalidate()
                }
            }
            launch {
                allStations = runCatching {
                    getRadioStationsUseCase.execute(page = 1, searchName = null)
                        .also { 
                            Log.d("ketai", "all stations: ${it.joinToString { it.name }}")
                        }
                }.onFailure {
                    Timber.e(it, "failed to fetch the stations")
                }.getOrElse { emptyList() }
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
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                scope.cancel()
            }
        })
    }

    override fun onGetTemplate(): Template {
        val tabCallback = object : TabTemplate.TabCallback {
            override fun onTabSelected(tabContentId: String) {
                if (activeTab != tabContentId) {
                    activeTab = tabContentId
                    invalidate()
                }
            }
        }

        val browseTab = Tab.Builder()
            .setTitle("Browse")
            .setIcon(CarIcon.APP_ICON)
            .setContentId(TAB_BROWSE)
            .build()

        val pinnedTab = Tab.Builder()
            .setTitle("Pinned")
            .setIcon(CarIcon.APP_ICON)
            .setContentId(TAB_PINNED)
            .build()

        val searchTab = Tab.Builder()
            .setTitle("Search")
            .setIcon(CarIcon.APP_ICON)
            .setContentId(TAB_SEARCH)
            .build()

        val activeTemplate = when (activeTab) {
            TAB_PINNED -> buildPinnedTemplate()
            TAB_SEARCH -> buildSearchTemplate()
            else -> buildBrowseTemplate()
        }

        return TabTemplate.Builder(tabCallback)
            .setHeaderAction(Action.APP_ICON)
            .addTab(browseTab)
            .addTab(pinnedTab)
            .addTab(searchTab)
            .setActiveTabContentId(activeTab)
            .setTabContents(TabContents.Builder(activeTemplate).build())
            .build()
    }

    private fun buildBrowseTemplate(): Template {
        if (isLoading) {
            return ListTemplate.Builder()
                .setLoading(true)
                .build()
        }

        if (pinnedStations.isEmpty() && allStations.isEmpty()) {
            return MessageTemplate.Builder("No stations available yet.")
                .setTitle("247FM")
                .build()
        }

        val pinnedItemList = ItemList.Builder().apply {
            pinnedStations.take(6).forEach { station ->
                addItem(buildStationRow(station, RadioPlayerController.PlaybackContext.Type.PINNED))
            }
        }.build()

        val allItemList = ItemList.Builder().apply {
            allStations.take(6).forEach { station ->
                addItem(buildStationRow(station, RadioPlayerController.PlaybackContext.Type.DEFAULT))
            }
        }.build()

        return ListTemplate.Builder().apply {
            if (pinnedStations.isNotEmpty()) {
                addSectionedList(SectionedItemList.create(pinnedItemList, "PINNED"))
            }
            if (allStations.isNotEmpty()) {
                addSectionedList(SectionedItemList.create(allItemList, "ALL STATIONS"))
            }
            if (!isLoading && pinnedStations.isEmpty() && allStations.isEmpty()) {
                setSingleList(ItemList.Builder().setNoItemsMessage("No stations found").build())
            }
            setActionStrip(buildActionStrip())
        }.build()
    }

    private fun buildPinnedTemplate(): Template {
        if (pinnedStations.isEmpty()) {
            return MessageTemplate.Builder("No pinned stations yet.")
                .setTitle("Pinned")
                .build()
        }

        val gridItemList = ItemList.Builder().apply {
            pinnedStations.forEach { station ->
                addItem(
                    GridItem.Builder()
                        .setTitle(station.name)
                        .setImage(CarIcon.APP_ICON)
                        .setOnClickListener { onStationSelected(station, RadioPlayerController.PlaybackContext.Type.PINNED) }
                        .build()
                )
            }
        }.build()

        return GridTemplate.Builder()
            .setSingleList(gridItemList)
            .setTitle("Pinned")
            .build()
    }

    private fun buildSearchTemplate(): Template {
        val itemList = ItemList.Builder().apply {
            if (searchQuery.isEmpty()) {
                listOf("pop", "rock", "news", "jazz").forEach { genre ->
                    addItem(
                        Row.Builder()
                            .setTitle(genre.replaceFirstChar { it.uppercase() })
                            .setOnClickListener { performSearch(genre) }
                            .build()
                    )
                }
            } else {
                searchResults.take(6).forEach { station ->
                    addItem(buildStationRow(station, RadioPlayerController.PlaybackContext.Type.SEARCH))
                }
            }
        }.build()

        return SearchTemplate.Builder(object : SearchTemplate.SearchCallback {
            override fun onSearchTextChanged(searchText: String) {
                performSearch(searchText)
            }

            override fun onSearchSubmitted(searchText: String) {
                performSearch(searchText)
            }
        })
            .setInitialSearchText(searchQuery)
            .setItemList(itemList)
            .build()
    }

    private fun performSearch(query: String) {
        searchQuery = query
        scope.launch {
            searchResults = runCatching {
                getRadioStationsUseCase.execute(page = 1, searchName = query.takeIf { it.isNotBlank() })
                    .also { 
                        Log.d("ketai", "search result: ${it.joinToString { it.name }}")
                    }
            }.onFailure {
                Timber.e(it, "Failed to search station $query")
            }.getOrElse { emptyList() }
            invalidate()
        }
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
