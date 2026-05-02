package io.github.agimaulana.radio.feature.auto.screen

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.hardware.CarHardwareManager
import androidx.car.app.hardware.info.CarInfo
import androidx.car.app.hardware.common.CarValue
import androidx.car.app.hardware.common.OnCarDataAvailableListener
import androidx.car.app.hardware.info.Speed
import androidx.car.app.model.Action
import androidx.car.app.model.ActionStrip
import androidx.car.app.model.ItemList
import androidx.car.app.model.MessageTemplate
import androidx.car.app.model.ParkedOnlyOnClickListener
import androidx.car.app.model.Row
import androidx.car.app.model.SearchTemplate
import androidx.car.app.model.Template
import androidx.core.content.ContextCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import io.github.agimaulana.radio.core.radioplayer.RadioPlayerController
import io.github.agimaulana.radio.domain.api.entity.RadioStation
import io.github.agimaulana.radio.domain.api.usecase.GetPinnedStationsUseCase
import io.github.agimaulana.radio.domain.api.usecase.GetRadioStationsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SearchScreen(
    carContext: CarContext,
    private val getRadioStationsUseCase: GetRadioStationsUseCase,
    private val getPinnedStationsUseCase: GetPinnedStationsUseCase,
    private val playerController: StateFlow<RadioPlayerController?>,
) : Screen(carContext) {

    private sealed interface SearchState {
        object Voice : SearchState
        object ParkedGate : SearchState
        data class TextSearch(val query: String, val results: List<RadioStation>) : SearchState
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var state: SearchState = SearchState.Voice
    private var pinnedIds: Set<String> = emptySet()
    private var isParked = true

    private val speedListener = OnCarDataAvailableListener<Speed> { speed ->
        val rawSpeed = speed.rawSpeedMetersPerSecond
        if (rawSpeed.status == CarValue.STATUS_SUCCESS) {
            val rawSpeedValue = rawSpeed.value
            // Threshold of 0.5 m/s (~1.8 km/h) to consider the car moving
            val newIsParked = rawSpeedValue == null || rawSpeedValue < 0.5f
            if (newIsParked != isParked) {
                isParked = newIsParked
                handleCarStateChange()
            }
        }
    }

    private fun handleCarStateChange() {
        if (isParked) {
            if (state is SearchState.Voice) {
                state = SearchState.ParkedGate
            }
        }
        invalidate()
    }

    private fun observeSpeed() {
        runCatching {
            val carHardwareManager = carContext.getCarService(CarHardwareManager::class.java)
            carHardwareManager.carInfo.addSpeedListener(
                ContextCompat.getMainExecutor(carContext),
                speedListener
            )
        }
    }

    init {
        scope.launch {
            getPinnedStationsUseCase.execute().collect { pinned ->
                pinnedIds = pinned.map { it.stationUuid }.toSet()
                invalidate()
            }
        }
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                val permissions = listOf(
                    "android.car.permission.CAR_SPEED",
                    "com.google.android.gms.permission.CAR_SPEED"
                )
                carContext.requestPermissions(permissions) { granted, _ ->
                    if (granted.contains("android.car.permission.CAR_SPEED") ||
                        granted.contains("com.google.android.gms.permission.CAR_SPEED")
                    ) {
                        observeSpeed()
                    }
                }
            }

            override fun onPause(owner: LifecycleOwner) {
                runCatching {
                    val carHardwareManager = carContext.getCarService(CarHardwareManager::class.java)
                    carHardwareManager.carInfo.removeSpeedListener(speedListener)
                }
            }

            override fun onDestroy(owner: LifecycleOwner) {
                scope.cancel()
            }
        })
    }

    override fun onGetTemplate(): Template {
        val s = state
        if (!isParked && s is SearchState.TextSearch) {
            return buildSafetyLockTemplate(s)
        }
        return when (s) {
            is SearchState.Voice -> buildVoiceTemplate()
            is SearchState.ParkedGate -> buildParkedGateTemplate()
            is SearchState.TextSearch -> buildTextSearchTemplate(s.query, s.results)
        }
    }

    // State 1 — driving, voice only
    private fun buildVoiceTemplate(): MessageTemplate {
        return MessageTemplate.Builder("Say a station name, genre, or city")
            .setTitle("Search")
            .setHeaderAction(Action.BACK)
            .addAction(
                Action.Builder()
                    .setTitle("\"Play jazz Jakarta\"")
                    .setOnClickListener(
                        ParkedOnlyOnClickListener.create {
                            state = SearchState.ParkedGate
                            invalidate()
                        }
                    )
                    .build()
            )
            .addAction(
                Action.Builder()
                    .setTitle("\"Open Most FM\"")
                    .setOnClickListener(
                        ParkedOnlyOnClickListener.create {
                            state = SearchState.ParkedGate
                            invalidate()
                        }
                    )
                    .build()
            )
            .setActionStrip(
                ActionStrip.Builder()
                    .addAction(
                        Action.Builder()
                            .setTitle("Type when parked")
                            .setOnClickListener(
                                ParkedOnlyOnClickListener.create {
                                    state = SearchState.ParkedGate
                                    invalidate()
                                }
                            )
                            .build()
                    )
                    .build()
            )
            .build()
    }

    // State 2 — just parked, confirmation gate
    private fun buildParkedGateTemplate(): MessageTemplate {
        return MessageTemplate.Builder(
            "Text search is now available.\nTap below to type or keep using voice."
        )
            .setTitle("Vehicle Parked")
            .setHeaderAction(Action.BACK)
            .addAction(
                Action.Builder()
                    .setTitle("Type to search")
                    .setOnClickListener {
                        state = SearchState.TextSearch("", emptyList())
                        invalidate()
                    }
                    .build()
            )
            .addAction(
                Action.Builder()
                    .setTitle("Use voice")
                    .setOnClickListener {
                        state = SearchState.Voice
                        invalidate()
                    }
                    .build()
            )
            .build()
    }

    // States 3 + 4 — empty keyboard / typing with live results
    private fun buildTextSearchTemplate(query: String, results: List<RadioStation>): Template {
        val itemList = ItemList.Builder().apply {
            if (query.isEmpty()) {
                // State 3: show genre suggestions as tappable rows
                listOf("pop", "rock", "news", "dangdut", "koplo", "jazz").forEach { genre ->
                    addItem(
                        Row.Builder()
                            .setTitle(genre.replaceFirstChar { it.uppercase() })
                            .setOnClickListener {
                                scope.launch {
                                    val genreResults = runCatching {
                                        getRadioStationsUseCase.execute(page = 1, searchName = genre)
                                    }.getOrElse { emptyList() }
                                    state = SearchState.TextSearch(genre, genreResults)
                                    invalidate()
                                }
                            }
                            .build()
                    )
                }
            } else {
                // State 4: live results with pinned badge
                if (results.isEmpty()) {
                    setNoItemsMessage("No stations found for \"$query\"")
                }
                results.take(6).forEach { station ->
                    val isPinned = station.stationUuid in pinnedIds
                    addItem(
                        Row.Builder()
                            .setTitle(station.name)
                            .addText(buildString {
                                append(station.tags.firstOrNull() ?: "")
                                if (isPinned) append(" · Pinned")
                            })
                            .setOnClickListener {
                                scope.launch {
                                    playerController.value?.startPlayback(
                                        items = listOf(station.toRadioMediaItem()),
                                        startIndex = 0,
                                        context = RadioPlayerController.PlaybackContext(
                                            type = RadioPlayerController.PlaybackContext.Type.SEARCH,
                                            query = query,
                                        )
                                    )
                                }
                                screenManager.pop()
                            }
                            .build()
                    )
                }
            }
        }.build()

        return SearchTemplate.Builder(
            object : SearchTemplate.SearchCallback {
                override fun onSearchTextChanged(searchText: String) {
                    scope.launch {
                        val searchResults = runCatching {
                            getRadioStationsUseCase.execute(page = 1, searchName = searchText.takeIf { it.isNotBlank() })
                        }.getOrElse { emptyList() }
                        state = SearchState.TextSearch(searchText, searchResults)
                        invalidate()
                    }
                }

                override fun onSearchSubmitted(searchText: String) {
                    scope.launch {
                        val searchResults = runCatching {
                            getRadioStationsUseCase.execute(page = 1, searchName = searchText.takeIf { it.isNotBlank() })
                        }.getOrElse { emptyList() }
                        state = SearchState.TextSearch(searchText, searchResults)
                        invalidate()
                    }
                }
            }
        )
            .setInitialSearchText(query)
            .setShowKeyboardByDefault(true)
            .setHeaderAction(Action.BACK)
            .setItemList(itemList)
            .build()
    }

    // State 5 — car started moving again, keyboard locked
    private fun buildSafetyLockTemplate(lastState: SearchState.TextSearch): Template {
        val itemList = ItemList.Builder().apply {
            if (lastState.results.isEmpty()) {
                setNoItemsMessage("Results from last search")
            }
            lastState.results.take(6).forEach { station ->
                val isPinned = station.stationUuid in pinnedIds
                addItem(
                    Row.Builder()
                        .setTitle(station.name)
                        .addText(buildString {
                            append(station.tags.firstOrNull() ?: "")
                            if (isPinned) append(" · Pinned")
                        })
                        .setOnClickListener {
                            scope.launch {
                                playerController.value?.startPlayback(
                                    items = listOf(station.toRadioMediaItem()),
                                    startIndex = 0,
                                    context = RadioPlayerController.PlaybackContext(
                                        type = RadioPlayerController.PlaybackContext.Type.SEARCH,
                                        query = lastState.query,
                                    )
                                )
                            }
                            screenManager.pop()
                        }
                        .build()
                )
            }
        }.build()

        return SearchTemplate.Builder(
            object : SearchTemplate.SearchCallback {
                // Driving — platform blocks text input, callbacks won't fire
                override fun onSearchTextChanged(searchText: String) = Unit
                override fun onSearchSubmitted(searchText: String) = Unit
            }
        )
            .setInitialSearchText(lastState.query)
            .setShowKeyboardByDefault(false)
            .setHeaderAction(Action.BACK)
            .setItemList(itemList)
            .setActionStrip(
                ActionStrip.Builder()
                    .addAction(
                        Action.Builder()
                            .setTitle("⚠ Driving — use voice")
                            .setOnClickListener {
                                state = SearchState.Voice
                                invalidate()
                            }
                            .build()
                    )
                    .build()
            )
            .build()
    }
}
