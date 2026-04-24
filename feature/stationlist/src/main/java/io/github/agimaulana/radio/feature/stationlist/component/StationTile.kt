package io.github.agimaulana.radio.feature.stationlist.component

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import io.github.agimaulana.radio.core.design.RadioTheme
import io.github.agimaulana.radio.core.design.theme.PreviewTheme
import io.github.agimaulana.radio.feature.stationlist.R
import io.github.agimaulana.radio.feature.stationlist.StationListViewModel.UiState.Station
import timber.log.Timber

@Composable
internal fun StationTile(
    station: Station,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
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
                .clickable(onClick = onClick)
                .padding(vertical = 16.dp)
                .padding(start = 16.dp, end = 48.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            details(
                station = station,
                contentColor = contentColor,
            )
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
    RadioImage(
        stationName = station.name,
        imageUrl = station.imageUrl,
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

@Composable
private fun RadioImage(
    stationName: String,
    imageUrl: String,
    modifier: Modifier = Modifier,
    size: Dp = 96.dp,
    placeholderSize: Dp = 48.dp
) {
    val painter = rememberAsyncImagePainter(
        model = imageUrl,
        placeholder = painterResource(id = R.drawable.station_default),
        error = painterResource(id = R.drawable.station_default),
        onLoading = {
            Timber.tag("StationTile").d("Loading image for %s: %s", stationName, imageUrl)
        },
        onSuccess = {
            Timber.tag("StationTile").d("Image loaded for %s: %s", stationName, imageUrl)
        },
        onError = {
            Timber.tag("StationTile").d("Error loading image for %s: %s", stationName, imageUrl)
        }
    )
    Box {
        when (painter.state) {
            is AsyncImagePainter.State.Success -> {
                Image(
                    modifier = modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(RadioTheme.colors.muted)
                        .size(size),
                    painter = painter,
                    contentDescription = stationName,
                    contentScale = ContentScale.Fit,
                )
            }

            else -> {
                Box(
                    modifier = modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(RadioTheme.colors.muted)
                        .size(size)
                ) {
                    Image(
                        modifier = Modifier
                            .size(placeholderSize)
                            .align(Alignment.Center),
                        painter = painter,
                        contentDescription = stationName,
                        colorFilter = ColorFilter.tint(RadioTheme.colors.mutedForeground),
                        contentScale = ContentScale.Fit,
                    )
                }
            }
        }
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
                isPlaying = false
            ),
            Station(
                serverUuid = "uuid",
                name = "24.7 FM",
                genre = "Pop",
                imageUrl = "",
                streamUrl = "",
                isBuffering = false,
                isPlaying = true
            ),
        )

}
