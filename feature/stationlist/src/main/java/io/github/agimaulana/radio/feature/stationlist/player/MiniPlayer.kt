package io.github.agimaulana.radio.feature.stationlist.player

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import io.github.agimaulana.radio.core.design.theme.PreviewTheme
import io.github.agimaulana.radio.feature.stationlist.R
import io.github.agimaulana.radio.feature.stationlist.StationListViewModel.UiState.Station

@Composable
internal fun MiniPlayer(
    station: Station,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth()
            .background(MaterialTheme.colorScheme.background),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = rememberAsyncImagePainter(
                model = station.imageUrl,
                placeholder = painterResource(id = R.drawable.station_default),
                error = painterResource(id = R.drawable.station_default),
            ),
            contentDescription = station.name,
            modifier = Modifier.size(64.dp)
        )

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = station.name,
                style = MaterialTheme.typography.titleMedium,
            )

            Text(
                text = station.genre,
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        AnimatedContent(
            targetState = station.isBuffering,
            label = "MiniPlayer.Buffering"
        ) { isBuffering ->
            if (isBuffering) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 2.dp,
                )
            } else {
                if (station.isPlaying) {
                    IconButton(
                        onClick = onPause
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_pause),
                            contentDescription = null,
                        )
                    }
                } else {
                    IconButton(
                        onClick = onPlay,
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_play),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.surfaceTint)
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun MiniPlayerPreview() {
    PreviewTheme {
        MiniPlayer(
            station = Station(
                serverUuid = "uuid",
                name = "24.7 FM",
                genre = "Pop",
                imageUrl = "",
                streamUrl = "",
                isBuffering = false,
                isPlaying = false
            ),
            onPlay = {},
            onPause = {},
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun MiniPlayerSheetModePreview() {
    PreviewTheme {
        ModalBottomSheet(
            onDismissRequest = {},
            dragHandle = null,
        ) {
            MiniPlayer(
                station = Station(
                    serverUuid = "uuid",
                    name = "24.7 FM",
                    genre = "Pop",
                    imageUrl = "",
                    streamUrl = "",
                    isBuffering = false,
                    isPlaying = false
                ),
                onPlay = {},
                onPause = {},
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

