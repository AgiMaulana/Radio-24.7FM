package io.github.agimaulana.radio.feature.stationlist.player

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import io.github.agimaulana.radio.core.design.RadioTheme
import io.github.agimaulana.radio.core.design.theme.PreviewTheme
import io.github.agimaulana.radio.feature.stationlist.R
import io.github.agimaulana.radio.feature.stationlist.StationListViewModel.UiState.Station

@Composable
internal fun FullPlayer(
    station: Station,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onStop: () -> Unit,
    onCollapse: () -> Unit,
    modifier: Modifier = Modifier,
    progress: Float = 1f,
) {
    val density = LocalDensity.current
    val statusBarHeight = WindowInsets.statusBars.getTop(density)
    val dynamicTopPadding = with(density) { (statusBarHeight * progress).toDp() }
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        IconButton(
            onClick = onCollapse,
            modifier = Modifier.align(Alignment.TopEnd)
                .padding(top = dynamicTopPadding)
                .alpha(progress)
        ) {
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Close")
        }

        PlayerControls(
            progress = progress,
            station = station,
            onPlay = onPlay,
            onPause = onPause,
            onStop = onStop,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
private fun PlayerControls(
    progress: Float,
    station: Station,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(64.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        RadioImage(
            progress = progress,
            stationName = station.name,
            imageUrl = station.imageUrl
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = station.name,
                fontSize = 24.sp,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = station.genre,
                fontSize = 16.sp,
                color = RadioTheme.colors.secondaryForeground
            )
        }

        Box {
            AnimatedContent(
                targetState = station.isBuffering,
                label = "FullPlayer.Buffering"
            ) { buffering ->
                if (buffering) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(64.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 2.dp,
                    )
                } else {
                    PlayerController(
                        isPlaying = station.isPlaying,
                        onPlay = onPlay,
                        onPause = onPause,
                        onStop = onStop
                    )
                }
            }
        }
    }
}

@Composable
private fun RadioImage(
    progress: Float,
    stationName: String,
    imageUrl: String,
    modifier: Modifier = Modifier,
) {
    val imageScale = 0.5f + (0.5f * progress)
    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = imageScale
                scaleY = imageScale
                alpha = progress.coerceIn(0f, 1f)
            }
            .shadow(12.dp, RoundedCornerShape(16.dp))
            .background(RadioTheme.colors.muted)
            .size(256.dp),
    ) {
        Image(
            painter = rememberAsyncImagePainter(
                model = imageUrl,
                placeholder = painterResource(id = R.drawable.station_default),
                error = painterResource(id = R.drawable.station_default),
            ),
            contentDescription = stationName,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun PlayerController(
    isPlaying: Boolean,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        Box(
            modifier = Modifier.size(64.dp)
                .clip(CircleShape)
                .background(RadioTheme.colors.ring.copy(alpha = 0.5f))
                .clickable(
                    onClick = if (isPlaying) onPause else onPlay
                )
        ) {
            if (isPlaying) {
                Image(
                    painter = painterResource(id = R.drawable.ic_pause),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(RadioTheme.colors.ring),
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.ic_play),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(RadioTheme.colors.ring),
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        Box(
            modifier = Modifier.size(64.dp)
                .clip(CircleShape)
                .clickable(onClick = onStop)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_circle_stop),
                contentDescription = null,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@Preview(
    showSystemUi = true,
    showBackground = true,
)
@Composable
private fun FullPlayerPreview() {
    PreviewTheme {
        FullPlayer(
            station = Station(
                serverUuid = "uuid",
                name = "24.7 FM",
                genre = "Pop",
                imageUrl = "",
                streamUrl = "",
                isBuffering = true,
                isPlaying = false
            ),
            onPlay = {},
            onPause = {},
            onStop = {},
            onCollapse = {},
        )
    }
}

@OptIn(
    ExperimentalMaterial3Api::class
)
@Preview
@Composable
private fun FullPlayerSheetModePreview() {
    PreviewTheme {
        ModalBottomSheet(
            onDismissRequest = {},
        ) {
            FullPlayer(
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
                onStop = {},
                onCollapse = {},
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}