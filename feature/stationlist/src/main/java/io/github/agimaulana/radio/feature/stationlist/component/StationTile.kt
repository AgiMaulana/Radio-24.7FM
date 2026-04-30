package io.github.agimaulana.radio.feature.stationlist.component

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.agimaulana.radio.core.design.RadioTheme
import io.github.agimaulana.radio.core.design.theme.PreviewTheme
import io.github.agimaulana.radio.feature.stationlist.R
import io.github.agimaulana.radio.feature.stationlist.StationListViewModel.UiState.Station

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun StationTile(
    station: Station,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
) {
    val border = if (station.isPlaying) RadioTheme.colors.ring else RadioTheme.colors.border
    val background = if (station.isPlaying) {
        MaterialTheme.colorScheme.background
    } else {
        MaterialTheme.colorScheme.surface
    }

    val contentColor = contentColorFor(background)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = 1.dp,
                color = border,
                shape = RoundedCornerShape(16.dp)
            )
            .background(background)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick
                )
                .padding(vertical = 16.dp)
                .padding(start = 16.dp, end = 48.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            details(
                station = station,
                contentColor = contentColor,
            )
        }

        if (station.isPinned) {
            Box(
                modifier = Modifier
                    .offset(x = 8.dp, y = (-8).dp)
                    .align(Alignment.TopStart)
                    .size(28.dp)
                    .background(RadioTheme.colors.ring, CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.Icon(
                    painter = painterResource(id = R.drawable.ic_star_filled),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        AnimatedVisibility(
            visible = station.isPlaying,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp),
        ) {
            PlayingWaveIndicator()
        }
    }
}

@Composable
private fun RowScope.details(
    station: Station,
    contentColor: Color
) {
    StationImage(
        stationName = station.name,
        imageUrl = station.imageUrl,
        size = 96.dp,
        placeholderSize = 48.dp,
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = station.name,
            fontSize = 16.sp,
            style = RadioTheme.typography.stationTitle,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = station.genre,
            fontSize = 14.sp,
            color = contentColor,
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Composable
private fun StationTileLightModePreview(
    @PreviewParameter(StationPreviewProvider::class)
    station: Station
) {
    PreviewTheme {
        StationTile(station = station)
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun StationTileDarkModePreview(
    @PreviewParameter(StationPreviewProvider::class) station: Station
) {
    PreviewTheme {
        StationTile(station = station)
    }
}

internal class StationPreviewProvider : PreviewParameterProvider<Station> {
    override val values: Sequence<Station>
        get() = sequenceOf(
            Station(
                serverUuid = "uuid",
                name = "24.7 FM",
                genre = "Pop",
                imageUrl = "",
                streamUrl = "",
                isBuffering = false,
                isPlaying = false,
                isPinned = false
            ),
            Station(
                serverUuid = "uuid",
                name = "24.7 FM",
                genre = "Pop",
                imageUrl = "",
                streamUrl = "",
                isBuffering = false,
                isPlaying = true,
                isPinned = true
            ),
        )

}
