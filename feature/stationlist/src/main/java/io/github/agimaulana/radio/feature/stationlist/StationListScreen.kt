package io.github.agimaulana.radio.feature.stationlist

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.agimaulana.radio.core.design.GlassPlayerState
import io.github.agimaulana.radio.core.design.GlassSlidingPlayerLayout
import io.github.agimaulana.radio.core.design.RadioTheme
import io.github.agimaulana.radio.core.design.rememberGlassPlayerState
import io.github.agimaulana.radio.core.design.rememberMultiplePermissionsState
import io.github.agimaulana.radio.core.design.theme.PreviewTheme
import io.github.agimaulana.radio.feature.stationlist.StationListViewModel.Action
import io.github.agimaulana.radio.feature.stationlist.StationListViewModel.UiState
import io.github.agimaulana.radio.feature.stationlist.StationListViewModel.UiState.Station
import io.github.agimaulana.radio.feature.stationlist.component.LazyRadioStationList
import io.github.agimaulana.radio.feature.stationlist.component.LocationPermissionBottomSheet
import io.github.agimaulana.radio.feature.stationlist.component.StationContextMenu
import io.github.agimaulana.radio.feature.stationlist.player.BufferingIcon
import io.github.agimaulana.radio.feature.stationlist.player.FullPlayer
import io.github.agimaulana.radio.feature.stationlist.player.MiniPlayer
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.launch

internal data class ToolbarDimensions(
    val expandedHeight: Dp,
    val collapsedHeight: Dp,
    val progress: Float
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StationListRoute(
    viewModel: StationListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val playerState = rememberGlassPlayerState(peekHeight = 80.dp)
    val density = LocalDensity.current
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val toolbarDimensions = rememberToolbarDimensions(
        density = density,
        statusBarPadding = statusBarPadding,
        hasPinnedStations = uiState.pinnedStations.isNotEmpty()
    )
    val contextMenuState = rememberStationContextMenuState()

    fun resolveLocationPermission(isGranted: Boolean) {
        viewModel.onAction(Action.OnLocationPermissionGranted(isGranted))
    }

    val locationPermissionState = rememberMultiplePermissionsState(
        android.Manifest.permission.ACCESS_COARSE_LOCATION,
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        onPermissionResolved = ::resolveLocationPermission
    )

    BackHandler(enabled = playerState.canCollapse) {
        viewModel.onAction(Action.CollapsePlayer(source = "back_press"))
        playerState.collapse()
    }

    LaunchedEffect(Unit) {
        viewModel.init(
            hasLocationPermission = locationPermissionState.allGranted,
            hasAskedPermission = locationPermissionState.hasAskedPermission,
            shouldShowRationale = locationPermissionState.shouldShowRationale
        )
    }

    LaunchedEffect(locationPermissionState.allGranted, uiState.locationPermissionResolved) {
        if (locationPermissionState.allGranted && !uiState.locationPermissionResolved) {
            resolveLocationPermission(isGranted = true)
        }
    }

    StationListScreen(
        uiState = uiState,
        playerState = playerState,
        toolbarDimensions = toolbarDimensions,
        contextMenuState = contextMenuState,
        onAction = viewModel::onAction,
        showLocationPermissionSheet = uiState.showLocationPermissionSheet,
        onLaunchLocationPermissionRequest = {
            locationPermissionState.launchPermissionRequest()
        },
        onDismissLocationPermission = {
            resolveLocationPermission(isGranted = false)
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StationListScreen(
    uiState: UiState,
    playerState: GlassPlayerState,
    toolbarDimensions: ToolbarDimensionsHelper,
    contextMenuState: StationContextMenuState,
    showLocationPermissionSheet: Boolean,
    onLaunchLocationPermissionRequest: () -> Unit,
    onDismissLocationPermission: () -> Unit,
    modifier: Modifier = Modifier,
    onAction: (Action) -> Unit = {},
) {
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    if (showLocationPermissionSheet) {
        LocationPermissionBottomSheet(
            onAllowClick = onLaunchLocationPermissionRequest,
            onDismissRequest = onDismissLocationPermission,
        )
    }

    UpdateSystemBars()

    val nestedScrollConnection = remember(toolbarDimensions, listState) {
        StationListNestedScrollConnection(
            onScroll = { delta ->
                toolbarDimensions.onScroll(delta)
            },
            canScrollDown = { listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0 }
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        GlassSlidingPlayerLayout(
            state = playerState,
            modifier = Modifier.fillMaxSize(),
            miniPlayerContent = {
                StationMiniPlayer(uiState.selectedStation, onAction, playerState)
            },
            fullPlayerContent = { progress ->
                StationFullPlayer(progress, uiState, onAction, playerState)
            }
        ) {
            StationListContent(
                uiState = uiState,
                listState = listState,
                dims = toolbarDimensions.toData(),
                nestedScrollConnection = nestedScrollConnection,
                snackbarHostState = snackbarHostState,
                onAction = onAction,
                onLongClick = { station ->
                    contextMenuState.show(station)
                }
            )
        }

        StationContextMenu(
            showMenu = contextMenuState.showMenu,
            station = contextMenuState.station,
            useBottomSheet = contextMenuState.useBottomSheet,
            onDismiss = contextMenuState::dismiss,
            onAction = { action ->
                if (action is Action.UnpinStation) {
                    val stationToUnpin = contextMenuState.station
                    onAction(action)
                    if (stationToUnpin != null) {
                        scope.launch {
                            val result = snackbarHostState.showSnackbar(
                                message = "Removed ${stationToUnpin.name}",
                                actionLabel = "Undo",
                                duration = androidx.compose.material3.SnackbarDuration.Short
                            )
                            if (result == SnackbarResult.ActionPerformed) {
                                onAction(Action.PinStation(stationToUnpin))
                            }
                        }
                    }
                } else {
                    onAction(action)
                }
                contextMenuState.dismiss()
            }
        )
    }
}

@Composable
private fun UpdateSystemBars() {
    val view = LocalView.current
    SideEffect {
        val window = (view.context as Activity).window
        WindowCompat.getInsetsController(window, view).apply {
            isAppearanceLightStatusBars = false
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StationMiniPlayer(
    station: Station?,
    onAction: (Action) -> Unit,
    playerState: GlassPlayerState
) {
    station?.let {
        MiniPlayer(
            station = it,
            onPlay = { onAction(Action.Play(it)) },
            onPause = { onAction(Action.Pause(it)) },
            modifier = Modifier.clickable {
                onAction(Action.ExpandPlayer(source = "mini_player_tap"))
                playerState.expand()
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StationFullPlayer(
    progress: Float,
    uiState: UiState,
    onAction: (Action) -> Unit,
    playerState: GlassPlayerState
) {
    uiState.selectedStation?.let { station ->
        FullPlayer(
            progress = progress,
            station = station,
            playerColors = uiState.playerColors,
            featureFlag = uiState.featureFlag,
            onPlay = { onAction(Action.Play(station)) },
            onPause = { onAction(Action.Pause(station)) },
            onStop = {
                onAction(Action.CollapsePlayer(source = "stop_button"))
                playerState.collapse()
                onAction(Action.Stop(station))
            },
            onCollapse = {
                onAction(Action.CollapsePlayer(source = "collapse_button"))
                playerState.collapse()
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun StationListContent(
    uiState: UiState,
    listState: LazyListState,
    dims: ToolbarDimensions,
    nestedScrollConnection: NestedScrollConnection,
    snackbarHostState: SnackbarHostState,
    onAction: (Action) -> Unit,
    onLongClick: (Station) -> Unit,
) {
    Scaffold(
        topBar = {
            StationListToolbar(
                dims = dims,
                uiState = uiState,
                onSearch = { onAction(Action.Search(it)) }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (uiState.isLoading && uiState.stations.isEmpty()) {
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                BufferingIcon(
                    modifier = Modifier.size(96.dp),
                    tint = RadioTheme.colors.primary,
                    tweenDurationMillis = 1500,
                )
            }
        } else {
            LazyRadioStationList(
                stations = uiState.stations,
                pinnedStations = uiState.pinnedStations,
                listState = listState,
                onClick = { onAction(Action.Click(it)) },
                onLongClick = onLongClick,
                onReachEnd = { onAction(Action.LoadMore) },
                contentPadding = PaddingValues(
                    top = lerp(dims.expandedHeight, dims.collapsedHeight, dims.progress) + 16.dp,
                    bottom = innerPadding.calculateBottomPadding() + 80.dp,
                ),
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(nestedScrollConnection)
            )
        }
    }
}

private fun StationListNestedScrollConnection(
    onScroll: (Float) -> Float,
    canScrollDown: () -> Boolean
): NestedScrollConnection = object : NestedScrollConnection {
    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
        val delta = available.y
        return if (delta < 0) Offset(0f, onScroll(delta)) else Offset.Zero
    }

    override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource
    ): Offset {
        val delta = available.y
        return if (delta > 0 && canScrollDown()) Offset(0f, onScroll(delta)) else Offset.Zero
    }
}

@Preview(showBackground = true)
@Composable
private fun StationListScreenPreview() {
    PreviewTheme {
        StationListScreen(
            uiState = UiState(
                showLocationPermissionSheet = false,
                locationName = "Jakarta, Indonesia",
                stations = persistentListOf(
                    Station(
                        serverUuid = "1",
                        name = "Radio 1",
                        genre = "Genre 1",
                        imageUrl = "",
                        streamUrl = "",
                        isBuffering = false,
                        isPlaying = false
                    )
                )
            ),
            playerState = rememberGlassPlayerState(peekHeight = 80.dp),
            toolbarDimensions = rememberToolbarDimensions(
                density = LocalDensity.current,
                statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
                hasPinnedStations = false
            ),
            contextMenuState = rememberStationContextMenuState(),
            showLocationPermissionSheet = false,
            onLaunchLocationPermissionRequest = {},
            onDismissLocationPermission = {},
        )
    }
}
