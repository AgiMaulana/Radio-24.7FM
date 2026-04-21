package io.github.agimaulana.radio.feature.stationlist

import android.app.Activity
import android.content.res.Configuration.UI_MODE_NIGHT_YES
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
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
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
import io.github.agimaulana.radio.core.design.theme.PreviewTheme
import io.github.agimaulana.radio.feature.stationlist.StationListViewModel.Action
import io.github.agimaulana.radio.feature.stationlist.StationListViewModel.UiState
import io.github.agimaulana.radio.feature.stationlist.StationListViewModel.UiState.Station
import io.github.agimaulana.radio.feature.stationlist.component.LazyRadioStationList
import io.github.agimaulana.radio.feature.stationlist.player.FullPlayer
import io.github.agimaulana.radio.feature.stationlist.player.MiniPlayer
import kotlinx.collections.immutable.persistentListOf

private const val EXPANDED_CONTENT_THRESHOLD = 0.2f
private val HeroBackgroundColor = Color(0xFF1C1A24)
private const val DarkScrimAlpha = 0.50f
private const val LightScrimAlpha = 0.82f

private fun isLightTheme(background: Color): Boolean {
    val luminance = 0.2126f * background.red + 0.7152f * background.green + 0.0722f * background.blue
    return luminance > 0.5f
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StationListRoute(
    viewModel: StationListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val playerState = rememberGlassPlayerState(peekHeight = 80.dp)

    BackHandler(enabled = playerState.canCollapse) {
        playerState.collapse()
    }

    LaunchedEffect(Unit) {
        viewModel.init()
    }

    StationListScreen(
        uiState = uiState,
        playerState = playerState,
        onAction = viewModel::onAction
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StationListScreen(
    uiState: UiState,
    playerState: GlassPlayerState,
    modifier: Modifier = Modifier,
    onAction: (Action) -> Unit = {},
) {
    val listState = rememberLazyListState()
    val density = LocalDensity.current

    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val expandedHeight = 300.dp + statusBarPadding
    val collapsedHeight = 64.dp + statusBarPadding

    val expandedHeightPx = with(density) { expandedHeight.toPx() }
    val collapsedHeightPx = with(density) { collapsedHeight.toPx() }
    val toolbarHeightRangePx = expandedHeightPx - collapsedHeightPx

    var toolbarOffsetPx by rememberSaveable { mutableStateOf(0f) }

    val progress = if (toolbarHeightRangePx > 0) {
        (toolbarOffsetPx / toolbarHeightRangePx).coerceIn(0f, 1f)
    } else {
        0f
    }
    val isLight = isLightTheme(MaterialTheme.colorScheme.background)

    val view = LocalView.current
    SideEffect {
        val window = (view.context as Activity).window
        WindowCompat.getInsetsController(window, view).apply {
            // Force light icons because the toolbar background is always dark on this screen
            isAppearanceLightStatusBars = false
        }
    }

    val nestedScrollConnection = remember(toolbarHeightRangePx) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                if (delta < 0) { // Scrolling up
                    val oldOffset = toolbarOffsetPx
                    toolbarOffsetPx = (toolbarOffsetPx - delta).coerceAtMost(toolbarHeightRangePx)
                    val consumed = oldOffset - toolbarOffsetPx
                    return Offset(0f, consumed)
                }
                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                val delta = available.y
                if (delta > 0 && listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0) {
                    val oldOffset = toolbarOffsetPx
                    toolbarOffsetPx = (toolbarOffsetPx - delta).coerceAtLeast(0f)
                    val consumedOffset = oldOffset - toolbarOffsetPx
                    return Offset(0f, consumedOffset)
                }
                return Offset.Zero
            }
        }
    }

    GlassSlidingPlayerLayout(
        state = playerState,
        modifier = modifier.fillMaxSize(),
        miniPlayerContent = { playerProgress ->
            uiState.selectedStation?.let { station ->
                MiniPlayer(
                    station = station,
                    onPlay = { onAction(Action.Play(station)) },
                    onPause = { onAction(Action.Pause(station)) },
                    modifier = Modifier.clickable { playerState.expand() }
                )
            }
        },
        fullPlayerContent = { playerProgress ->
            uiState.selectedStation?.let { station ->
                FullPlayer(
                    station = station,
                    onPlay = { onAction(Action.Play(station)) },
                    onPause = { onAction(Action.Pause(station)) },
                    onStop = { onAction(Action.Stop(station)) },
                    onCollapse = { playerState.collapse() },
                    progress = playerProgress
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                StationListToolbar(
                    progress = progress,
                    filterStationName = uiState.filterStationName,
                    stationCount = uiState.stations.size,
                    onSearch = { onAction(Action.Search(it)) },
                    expandedHeight = expandedHeight,
                    collapsedHeight = collapsedHeight
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            LazyRadioStationList(
                stations = uiState.stations,
                listState = listState,
                onClick = { onAction(Action.Click(it)) },
                contentPadding = PaddingValues(
                    top = lerp(expandedHeight, collapsedHeight, progress),
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

@Composable
private fun StationListToolbar(
    progress: Float,
    filterStationName: String?,
    stationCount: Int,
    onSearch: (String) -> Unit,
    expandedHeight: Dp,
    collapsedHeight: Dp,
    modifier: Modifier = Modifier
) {
    val currentHeight = lerp(expandedHeight, collapsedHeight, progress)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(currentHeight)
            .background(HeroBackgroundColor)
    ) {
        Image(
            painter = painterResource(id = R.drawable.collapsing_toolbar_background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alignment = Alignment.BottomCenter,
            modifier = Modifier
                .fillMaxWidth()
                .height(expandedHeight)
                .align(Alignment.BottomCenter)
                .graphicsLayer {
                    alpha = 1f - progress * 0.2f
                }
        )

        val isLight = isLightTheme(MaterialTheme.colorScheme.background)
        val scrimAlpha = if (isLight) LightScrimAlpha else DarkScrimAlpha

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
                    .offset(
                        y = lerp(155.dp, 20.dp, progress)
                    )
            )

            CustomSearchBar(
                value = filterStationName.orEmpty(),
                onValueChange = onSearch,
                placeholder = if (progress < 0.5f) "Search your favourite station..." else "Search...",
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = lerp(0.dp, 95.dp, progress))
                    .offset(y = lerp(205.dp, 8.dp, progress))
                    .fillMaxWidth()
            )

            val featureFlagEnabled = false // not enabled yet
            if (featureFlagEnabled && progress < EXPANDED_CONTENT_THRESHOLD) {
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(y = lerp(264.dp, 200.dp, progress))
                        .graphicsLayer { alpha = (1f - progress * 5f).coerceAtLeast(0f) },
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
                        text = "Jakarta, Indonesia",
                        color = RadioTheme.colors.primary,
                        style = RadioTheme.typography.stationTitle.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = " • $stationCount stations nearby",
                        color = Color.Gray,
                        style = RadioTheme.typography.stationTitle.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal
                        )
                    )
                }
            }
        }
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
                stations = persistentListOf(
                    Station(
                        serverUuid = "1",
                        name = "Radio 1",
                        genre = "Genre 1",
                        imageUrl = "",
                        streamUrl = "",
                        isBuffering = false,
                        isPlaying = false
                    ),
                    Station(
                        serverUuid = "2",
                        name = "Radio 2",
                        genre = "Genre 2",
                        imageUrl = "",
                        streamUrl = "",
                        isBuffering = false,
                        isPlaying = false
                    )
                )
            ),
            playerState = rememberGlassPlayerState(peekHeight = 80.dp)
        )
    }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun StationListScreenDarkPreview() {
    PreviewTheme {
        StationListScreen(
            uiState = UiState(
                stations = persistentListOf(
                    Station(
                        serverUuid = "1",
                        name = "Radio 1",
                        genre = "Genre 1",
                        imageUrl = "",
                        streamUrl = "",
                        isBuffering = false,
                        isPlaying = false
                    ),
                    Station(
                        serverUuid = "2",
                        name = "Radio 2",
                        genre = "Genre 2",
                        imageUrl = "",
                        streamUrl = "",
                        isBuffering = false,
                        isPlaying = false
                    )
                )
            ),
            playerState = rememberGlassPlayerState(peekHeight = 80.dp)
        )
    }
}
