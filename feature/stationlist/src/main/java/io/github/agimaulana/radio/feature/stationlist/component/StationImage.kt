package io.github.agimaulana.radio.feature.stationlist.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import io.github.agimaulana.radio.core.design.RadioTheme
import io.github.agimaulana.radio.feature.stationlist.R
import timber.log.Timber

@Composable
internal fun StationImage(
    stationName: String,
    imageUrl: String,
    modifier: Modifier = Modifier,
    size: Dp,
    cornerRadius: Dp = 8.dp,
    placeholderSize: Dp,
    imageContentScale: ContentScale = ContentScale.Fit,
) {
    val painter = rememberAsyncImagePainter(
        model = imageUrl,
        placeholder = painterResource(id = R.drawable.station_default),
        error = painterResource(id = R.drawable.station_default),
        onLoading = {
            Timber.tag("StationImage").d("Loading image for %s: %s", stationName, imageUrl)
        },
        onSuccess = {
            Timber.tag("StationImage").d("Image loaded for %s: %s", stationName, imageUrl)
        },
        onError = {
            Timber.tag("StationImage").d("Error loading image for %s: %s", stationName, imageUrl)
        }
    )

    when (painter.state) {
        is AsyncImagePainter.State.Success -> {
            Image(
                modifier = modifier
                    .clip(RoundedCornerShape(cornerRadius))
                    .background(RadioTheme.colors.muted)
                    .size(size),
                painter = painter,
                contentDescription = stationName,
                contentScale = imageContentScale,
            )
        }

        else -> {
            Box(
                modifier = modifier
                    .clip(RoundedCornerShape(cornerRadius))
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
