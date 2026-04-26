package io.github.agimaulana.radio.feature.stationlist

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
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
import io.github.agimaulana.radio.feature.stationlist.player.BufferingIcon
import io.github.agimaulana.radio.feature.stationlist.player.FullPlayer
import io.github.agimaulana.radio.feature.stationlist.player.MiniPlayer
import kotlinx.collections.immutable.persistentListOf
import timber.log.Timber

private const val EXPANDED_CONTENT_THRESHOLD = 0.2f
private val HeroBackgroundColor = Color(0xFF1C1A24)
private const val DarkScrimAlpha = 0.50f
private const val LightScrimAlpha = 0.82f

private data class ToolbarDimensions(
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

    // Provide an explicit callback so the permission state can be resolved from the
    // ActivityResult callback (the source of truth) — this avoids races with
    // snapshot-based permission checks on some OEM devices.
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

    // Safeguard for OEMs or race conditions where the ActivityResult
    // callback isn't delivered on cold start. If permission is granted
    // but the ViewModel hasn't recorded that fact, dispatch the resolved
    // permission.
    LaunchedEffect(locationPermissionState.allGranted, uiState.locationPermissionResolved) {
        if (locationPermissionState.allGranted && !uiState.locationPermissionResolved) {
            resolveLocationPermission(isGranted = true)
        }
    }

    StationListScreen(
        uiState = uiState,
        playerState = playerState,
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
    showLocationPermissionSheet: Boolean,
    onLaunchLocationPermissionRequest: () -> Unit,
    onDismissLocationPermission: () -> Unit,
    modifier: Modifier = Modifier,
    onAction: (Action) -> Unit = {},
) {
    val listState = rememberLazyListState()
    val density = LocalDensity.current

    if (showLocationPermissionSheet) {
        LocationPermissionBottomSheet(
            onAllowClick = onLaunchLocationPermissionRequest,
            onDismissRequest = onDismissLocationPermission,
        )
    }

    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val dims = calculateToolbarDimensions(density, statusBarPadding)

    UpdateSystemBars()

    val nestedScrollConnection = remember(dims) {
        StationListNestedScrollConnection(
            onScroll = { delta ->
                dims.onScroll(delta)
            },
            canScrollDown = { listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0 }
        )
    }

    GlassSlidingPlayerLayout(
        state = playerState,
        modifier = modifier.fillMaxSize(),
        miniPlayerContent = {
            StationMiniPlayer(uiState.selectedStation, onAction, playerState)
        },
        fullPlayerContent = { progress ->
            StationFullPlayer(progress, uiState, onAction, playerState)
        }
    ) {
        StationListContent(uiState, listState, dims.toData(), nestedScrollConnection, onAction)
    }
}

@Composable
private fun calculateToolbarDimensions(
    density: Density,
    statusBarPadding: Dp
): ToolbarDimensionsHelper {
    val expandedHeight = 300.dp + statusBarPadding
    val collapsedHeight = 64.dp + statusBarPadding
    val expandedHeightPx = with(density) { expandedHeight.toPx() }
    val collapsedHeightPx = with(density) { collapsedHeight.toPx() }
    val toolbarHeightRangePx = expandedHeightPx - collapsedHeightPx

    var toolbarOffsetPx by rememberSaveable { mutableStateOf(0f) }

    val progress = if (toolbarHeightRangePx > 0f) {
        (toolbarOffsetPx / toolbarHeightRangePx).coerceIn(0f, 1f)
    } else {
        0f
    }

    return ToolbarDimensionsHelper(
        expandedHeight,
        collapsedHeight,
        progress,
        onScroll = { delta ->
            val oldOffset = toolbarOffsetPx
            toolbarOffsetPx = (toolbarOffsetPx - delta).coerceIn(0f, toolbarHeightRangePx)
            oldOffset - toolbarOffsetPx
        }
    )
}

private class ToolbarDimensionsHelper(
    val expandedHeight: Dp,
    val collapsedHeight: Dp,
    val progress: Float,
    val onScroll: (Float) -> Float
) {
    fun toData() = ToolbarDimensions(expandedHeight, collapsedHeight, progress)
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
    onAction: (Action) -> Unit,
) {
    Scaffold(
        topBar = {
            StationListToolbar(
                dims = dims,
                uiState = uiState,
                onSearch = { onAction(Action.Search(it)) }
            )
        },
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
                listState = listState,
                onClick = { onAction(Action.Click(it)) },
                onReachEnd = { onAction(Action.LoadMore) },
                contentPadding = PaddingValues(
                    top = lerp(dims.expandedHeight, dims.collapsedHeight, dims.progress) + 16.dp,
                    bottom = innerPadding.calculateBottomPadding() + 80.dp,
                    start = 16.dp,
                    end = 16.dp
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

@Composable
private fun StationListToolbar(
    dims: ToolbarDimensions,
    uiState: UiState,
    onSearch: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentHeight = lerp(dims.expandedHeight, dims.collapsedHeight, dims.progress)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(currentHeight)
            .background(HeroBackgroundColor)
    ) {
        ToolbarBackground(dims.expandedHeight, dims.progress)

        val isLight = luminance(MaterialTheme.colorScheme.background) > 0.5f
        val scrimAlpha = if (isLight) LightScrimAlpha else DarkScrimAlpha

        ToolbarScrim(scrimAlpha)
        ToolbarContent(dims.progress, uiState, onSearch)
    }
}

private fun luminance(color: Color): Float =
    0.2126f * color.red + 0.7152f * color.green + 0.0722f * color.blue

@Composable
private fun ToolbarBackground(expandedHeight: Dp, progress: Float) {
    Image(
        painter = painterResource(id = R.drawable.collapsing_toolbar_background),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        alignment = Alignment.BottomCenter,
        modifier = Modifier
            .fillMaxWidth()
            .height(expandedHeight)
            .graphicsLayer { alpha = 1f - progress * 0.2f }
    )
}

@Composable
private fun ToolbarScrim(scrimAlpha: Float) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colorStops = arrayOf(
                        0.00f to Color.Black.copy(alpha = 0f),
                        0.50f to Color.Black.copy(alpha = scrimAlpha * 0.6f),
                        1.00f to Color.Black.copy(alpha = scrimAlpha * 0.85f),
                    )
                )
            )
    )
}

@Composable
private fun ToolbarContent(
    progress: Float,
    uiState: UiState,
    onSearch: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        val titleSize = (32 * (1 - progress) + 20 * progress).sp

        Text(
            text = "24.7 FM",
            style = RadioTheme.typography.stationTitle.copy(
                fontSize = titleSize,
                fontWeight = FontWeight.Bold,
                color = Color.White
            ),
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(y = lerp(155.dp, 20.dp, progress))
        )

        CustomSearchBar(
            value = uiState.filterStationName.orEmpty(),
            onValueChange = onSearch,
            placeholder = if (progress < 0.5f) "Search your favourite station..." else "Search...",
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = lerp(0.dp, 95.dp, progress))
                .offset(y = lerp(205.dp, 8.dp, progress))
                .fillMaxWidth()
        )

        if (progress < EXPANDED_CONTENT_THRESHOLD && uiState.locationName != null) {
            LocationLabel(
                locationName = uiState.locationName,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(y = lerp(264.dp, 200.dp, progress))
                    .graphicsLayer { alpha = (1f - progress * 5f).coerceAtLeast(0f) }
            )
        }
    }
}

@Composable
private fun LocationLabel(
    locationName: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(RadioTheme.colors.primary)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = locationName,
            color = RadioTheme.colors.primary,
            style = RadioTheme.typography.stationTitle.copy(
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        )
    }
}

@Composable
private fun CustomSearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.1f))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_search),
                contentDescription = null,
                tint = RadioTheme.colors.primary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(modifier = Modifier.weight(1f)) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        color = Color.White.copy(alpha = 0.5f),
                        style = RadioTheme.typography.stationTitle.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal
                        )
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    textStyle = RadioTheme.typography.stationTitle.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.White
                    ),
                    cursorBrush = SolidColor(RadioTheme.colors.primary),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
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
            showLocationPermissionSheet = false,
            onLaunchLocationPermissionRequest = {},
            onDismissLocationPermission = {},
        )
    }
}
