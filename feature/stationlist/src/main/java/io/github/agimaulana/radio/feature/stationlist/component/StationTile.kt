package io.github.agimaulana.radio.feature.stationlist.component

import android.content.res.Configuration
import android.util.Log
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

@Composable
internal fun StationTile(
    station: Station,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    val contentColor = MaterialTheme.colorScheme.onBackground
    val border = if (station.isPlaying) RadioTheme.colors.ring else RadioTheme.colors.border
    val darkTheme = isSystemInDarkTheme()
    val background = if (station.isPlaying) {
        if (darkTheme) RadioTheme.colors.accentForeground else RadioTheme.colors.accent.copy(alpha = 0.2f)
    } else RadioTheme.colors.background
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = 1.dp,
                color = border,
                shape = RoundedCornerShape(16.dp)
            )
            .background(background)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        details(
            station = station,
            contentColor = contentColor,
        )
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
        isPlaying = station.isPlaying
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
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 96.dp,
    placeholderSize: Dp = 48.dp
) {
    val painter = rememberAsyncImagePainter(
        model = imageUrl,
        placeholder = painterResource(id = R.drawable.station_default),
        error = painterResource(id = R.drawable.station_default),
        onLoading = {
            Log.d("ketai", "Loading image for ${stationName}: ${imageUrl}")
        },
        onSuccess = {
            Log.d("ketai", "Image loaded for ${stationName}: ${imageUrl}")
        },
        onError = {
            Log.d("ketai", "Error loading image for ${stationName}: ${imageUrl}")
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

        if (isPlaying) {
            Image(
                painter = painterResource(id = R.drawable.playing),
                contentDescription = null,
                colorFilter = ColorFilter.tint(RadioTheme.colors.ring),
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .size(size)
                    .align(Alignment.Center)
                    .background(Color.Black.copy(0.5f))
                    .padding(32.dp)
            )
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
