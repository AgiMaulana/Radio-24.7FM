package io.github.agimaulana.radio.feature.stationlist

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
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
    Scaffold { innerPadding ->
        GlassSlidingPlayerLayout(
            modifier = modifier.padding(innerPadding),
            state = playerState,
            peekHeight = 80.dp,
            mainContent = {
                LazyRadioStationList(
                    stations = uiState.stations,
                    onClick = {
                        onAction(Action.Click(it))
                    }
                )
            },
            miniPlayerContent = {
                if (uiState.playing != null) {
                    MiniPlayer(
                        station = uiState.playing,
                        onPlay = {
                            onAction(Action.Play(uiState.playing))
                        },
                        onPause = {
                            onAction(Action.Pause(uiState.playing))
                        },
                        modifier = Modifier
                            .clickable(
                                onClick = {
                                    playerState.expand()
                                }
                            )
                            .padding(16.dp)
                    )
                }
            },
            fullPlayerContent = { progress ->
                if (uiState.playing != null) {
                    FullPlayer(
                        progress = progress,
                        station = uiState.playing,
                        onPlay = {
                            onAction(Action.Play(uiState.playing))
                        },
                        onPause = {
                            onAction(Action.Pause(uiState.playing))
                        },
                        onStop = {
                            onAction(Action.Stop(uiState.playing))
                            playerState.collapse()
                        },
                        onCollapse = {
                            playerState.collapse()
                        },
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun StationListScreenPreview() {
    PreviewTheme {
        StationListScreen(
            uiState = UiState(
                stations = persistentListOf(
                    Station(
                        serverUuid = "uuid",
                        name = "24.7 FM",
                        genre = "Pop",
                        imageUrl = "",
                        isPlaying = false
                    ),
                    Station(
                        serverUuid = "uuid",
                        name = "24.7 FM",
                        genre = "Pop",
                        imageUrl = "",
                        isPlaying = true
                    ),
                )
            ),
            playerState = rememberGlassPlayerState(
                peekHeight = 80.dp
            )
        )
    }
}
