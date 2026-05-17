package io.github.agimaulana.radio.feature.stationlist.player

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .padding(horizontal = 16.dp),
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
                    .clip(
                        shape = RoundedCornerShape(8.dp)
                    )
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    )
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = station.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = station.genre,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            MiniPlayerPlayPauseButton(
                isBuffering = station.isBuffering,
                isPlaying = station.isPlaying,
                onClick = { isPlaying ->
                    if (isPlaying) {
                        onPause()
                    } else {
                        onPlay()
                    }
                }
            )
        }
        Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
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

@Preview(uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun MiniPlayerNightPreview() {
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
