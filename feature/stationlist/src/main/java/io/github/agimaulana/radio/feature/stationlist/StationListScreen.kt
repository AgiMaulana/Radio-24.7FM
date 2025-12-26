package io.github.agimaulana.radio.feature.stationlist

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.agimaulana.radio.core.design.GlassPlayerState
import io.github.agimaulana.radio.core.design.GlassSlidingPlayerLayout
import io.github.agimaulana.radio.core.design.rememberGlassPlayerState
import io.github.agimaulana.radio.core.design.theme.PreviewTheme
import io.github.agimaulana.radio.feature.stationlist.StationListViewModel.Action
import io.github.agimaulana.radio.feature.stationlist.StationListViewModel.UiState
import io.github.agimaulana.radio.feature.stationlist.StationListViewModel.UiState.Station
import io.github.agimaulana.radio.feature.stationlist.component.LazyRadioStationList
import io.github.agimaulana.radio.feature.stationlist.player.FullPlayer
import io.github.agimaulana.radio.feature.stationlist.player.MiniPlayer
import kotlinx.collections.immutable.persistentListOf

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
    Scaffold(
        modifier = Modifier.safeDrawingPadding()
    ) { innerPadding ->
        GlassSlidingPlayerLayout(
            state = playerState,
            scaffoldPadding = innerPadding,
            peekHeight = 80.dp,
            mainContent = {
                LazyRadioStationList(
                    stations = uiState.stations,
                    contentPadding = PaddingValues(bottom = 96.dp),
                    onClick = {
                        onAction(Action.Click(it))
                    },
                    modifier = modifier
                        .padding(innerPadding),
                )
            },
            miniPlayerContent = { progress ->
                if (uiState.selectedStation != null) {
                    MiniPlayer(
                        station = uiState.selectedStation,
                        onPlay = {
                            onAction(Action.Play(uiState.selectedStation))
                        },
                        onPause = {
                            onAction(Action.Pause(uiState.selectedStation))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .clickable(
                                onClick = {
                                    playerState.expand()
                                }
                            )
                    )
                }
            },
            fullPlayerContent = { progress ->
                if (uiState.selectedStation != null) {
                    FullPlayer(
                        progress = progress,
                        station = uiState.selectedStation,
                        onPlay = {
                            onAction(Action.Play(uiState.selectedStation))
                        },
                        onPause = {
                            onAction(Action.Pause(uiState.selectedStation))
                        },
                        onStop = {
                            playerState.collapse()
                            onAction(Action.Stop(uiState.selectedStation))
                        },
                        onCollapse = {
                            playerState.collapse()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(
    showBackground = true,
    showSystemUi = true
)
@Composable
private fun StationListScreenPreview() {
    PreviewTheme {
        val playerState = rememberGlassPlayerState(
            peekHeight = 80.dp,
        )

        LaunchedEffect(Unit) {
            playerState.collapse()
        }

        StationListScreen(
            playerState = playerState,
            uiState = UiState(
                stations = persistentListOf(
                    Station(
                        serverUuid = "uuid",
                        name = "24.7 FM",
                        genre = "Pop",
                        imageUrl = "",
                        streamUrl = "",
                        isBuffering = false,
                        isPlaying = false,
                    ),
                    Station(
                        serverUuid = "uuid",
                        name = "24.7 FM",
                        genre = "Pop",
                        imageUrl = "",
                        streamUrl = "",
                        isBuffering = false,
                        isPlaying = true,
                    ),
                    Station(
                        serverUuid = "uuid",
                        name = "24.7 FM",
                        genre = "Pop",
                        imageUrl = "",
                        streamUrl = "",
                        isBuffering = false,
                        isPlaying = false,
                    ),
                    Station(
                        serverUuid = "uuid",
                        name = "24.7 FM",
                        genre = "Pop",
                        imageUrl = "",
                        streamUrl = "",
                        isBuffering = false,
                        isPlaying = true,
                    ),
                    Station(
                        serverUuid = "uuid",
                        name = "24.7 FM",
                        genre = "Pop",
                        imageUrl = "",
                        streamUrl = "",
                        isBuffering = false,
                        isPlaying = false,
                    ),
                    Station(
                        serverUuid = "uuid",
                        name = "24.7 FM",
                        genre = "Pop",
                        imageUrl = "",
                        streamUrl = "",
                        isBuffering = false,
                        isPlaying = true,
                    ),
                    Station(
                        serverUuid = "uuid",
                        name = "24.7 FM",
                        genre = "Pop",
                        imageUrl = "",
                        streamUrl = "",
                        isBuffering = false,
                        isPlaying = false,
                    ),
                    Station(
                        serverUuid = "uuid",
                        name = "24.7 FM",
                        genre = "Pop",
                        imageUrl = "",
                        streamUrl = "",
                        isBuffering = false,
                        isPlaying = true,
                    ),
                    Station(
                        serverUuid = "uuid",
                        name = "24.7 FM",
                        genre = "Pop",
                        imageUrl = "",
                        streamUrl = "",
                        isBuffering = false,
                        isPlaying = false,
                    ),
                    Station(
                        serverUuid = "uuid",
                        name = "24.7 FM",
                        genre = "Pop",
                        imageUrl = "",
                        streamUrl = "",
                        isBuffering = false,
                        isPlaying = true,
                    ),
                    Station(
                        serverUuid = "uuid",
                        name = "24.7 FM",
                        genre = "Pop",
                        imageUrl = "",
                        streamUrl = "",
                        isBuffering = false,
                        isPlaying = false,
                    ),
                    Station(
                        serverUuid = "uuid",
                        name = "24.7 FM",
                        genre = "Pop",
                        imageUrl = "",
                        streamUrl = "",
                        isBuffering = false,
                        isPlaying = true,
                    ),
                ),
                selectedStation = Station(
                    serverUuid = "uuid",
                    name = "24.7 FM",
                    genre = "Pop",
                    imageUrl = "",
                    streamUrl = "",
                    isBuffering = false,
                    isPlaying = true,
                ),
            ),
        )
    }
}
