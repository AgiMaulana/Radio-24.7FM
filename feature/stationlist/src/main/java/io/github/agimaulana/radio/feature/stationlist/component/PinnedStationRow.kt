package io.github.agimaulana.radio.feature.stationlist.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import io.github.agimaulana.radio.core.design.theme.PreviewTheme
import io.github.agimaulana.radio.feature.stationlist.R
import io.github.agimaulana.radio.feature.stationlist.StationListViewModel.UiState.Station
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Composable
internal fun PinnedStationRow(
    pinnedStations: ImmutableList<Station>,
    onStationClick: (Station) -> Unit,
    onStationLongClick: (Station) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_star_filled),
                contentDescription = null,
                tint = Color(0xFFFF4081),
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = "PINNED",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = contentColorFor(MaterialTheme.colorScheme.background).copy(alpha = 0.72f),
                    letterSpacing = 1.5.sp
                )
            )
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(pinnedStations, key = { it.serverUuid }) { station ->
                PinnedStationChip(
                    station = station,
                    onClick = { onStationClick(station) },
                    onLongClick = { onStationLongClick(station) }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PinnedStationChip(
    station: Station,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(80.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White.copy(alpha = 0.1f))
                .then(
                    if (station.isPlaying) {
                        Modifier.border(2.dp, Color(0xFFFF4081), RoundedCornerShape(20.dp))
                    } else Modifier
                )
        ) {
            Image(
                painter = rememberAsyncImagePainter(
                    model = station.imageUrl,
                    placeholder = painterResource(id = R.drawable.station_default),
                    error = painterResource(id = R.drawable.station_default)
                ),
                contentDescription = station.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Star badge overlay
            Icon(
                painter = painterResource(id = R.drawable.ic_star_filled),
                contentDescription = null,
                tint = Color(0xFFFF4081),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(12.dp)
            )
        }
        Text(
            text = station.name,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = contentColorFor(MaterialTheme.colorScheme.background).copy(alpha = 0.8f)
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Preview
@Composable
private fun PinnedStationRowPreview() {
    PreviewTheme {
        PinnedStationRow(
            pinnedStations = persistentListOf(
                Station(
                    serverUuid = "1",
                    name = "Classic FM",
                    genre = "Classical",
                    imageUrl = "",
                    streamUrl = "",
                    isBuffering = false,
                    isPlaying = true,
                    isPinned = true
                ),
                Station(
                    serverUuid = "2",
                    name = "Jazz Radio",
                    genre = "Jazz",
                    imageUrl = "",
                    streamUrl = "",
                    isBuffering = false,
                    isPlaying = false,
                    isPinned = true
                )
            ),
            onStationClick = {},
            onStationLongClick = {}
        )
    }
}
