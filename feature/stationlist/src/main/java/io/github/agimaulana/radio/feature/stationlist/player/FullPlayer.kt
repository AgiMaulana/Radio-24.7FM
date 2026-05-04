package io.github.agimaulana.radio.feature.stationlist.player

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import io.github.agimaulana.radio.core.design.theme.PreviewTheme
import io.github.agimaulana.radio.feature.stationlist.R
import io.github.agimaulana.radio.feature.stationlist.StationListViewModel
import io.github.agimaulana.radio.feature.stationlist.StationListViewModel.UiState.Station
import io.github.agimaulana.radio.feature.stationlist.component.CastButton
import io.github.agimaulana.radio.feature.stationlist.component.PlayingWaveIndicator

@Composable
internal fun FullPlayer(
    station: Station,
    playerColors: PlayerColors,
    featureFlag: StationListViewModel.UiState.FeatureFlag,
    castState: io.github.agimaulana.radio.core.radioplayer.RadioPlayerController.CastState,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onStop: () -> Unit,
    onCollapse: () -> Unit,
    onCastClick: () -> Unit,
    modifier: Modifier = Modifier,
    progress: Float = 1f,
) {
    val density = LocalDensity.current
    val statusBarHeight = WindowInsets.statusBars.getTop(density)
    val dynamicTopPadding = with(density) { (statusBarHeight * progress).toDp() }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // Layer 1 — dynamic gradient from album art
        DynamicPlayerBackground(colors = playerColors)

        // Layer 2 — subtle dark overlay so text stays readable
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.35f))
        )

        // Layer 3 — UI Content
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            FullPlayerHeader(
                isBuffering = station.isBuffering,
                isMoreMenuEnabled = featureFlag.isMoreMenuEnabled,
                castState = castState,
                onCollapse = onCollapse,
                onCastClick = onCastClick,
                onMore = { /* TODO */ },
                progress = progress,
                dynamicTopPadding = dynamicTopPadding
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                RadioImage(
                    progress = progress,
                    stationName = station.name,
                    imageUrl = station.imageUrl
                )

                Spacer(modifier = Modifier.height(48.dp))

                Text(
                    text = station.name,
                    fontSize = 24.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                GenreChip(genre = station.genre)

                Spacer(modifier = Modifier.height(16.dp))

                if (station.isPlaying) {
                    PlayingWaveIndicator(
                        modifier = Modifier.size(width = 40.dp, height = 24.dp),
                        barCount = 5,
                        barWidth = 4.dp,
                        barSpacing = 3.dp
                    )
                } else {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            PlayerControls(
                isPlaying = station.isPlaying,
                isBuffering = station.isBuffering,
                isFavoriteEnabled = featureFlag.isFavoriteEnabled,
                onPlay = onPlay,
                onPause = onPause,
                onStop = onStop,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            if (featureFlag.isActionRowEnabled) {
                ActionRow(
                    onStream = { /* TODO */ },
                    onSleep = { /* TODO */ },
                    onShare = { /* TODO */ },
                    modifier = Modifier.padding(bottom = 48.dp)
                )
            } else {
                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}

@Composable
private fun FullPlayerHeader(
    isBuffering: Boolean,
    isMoreMenuEnabled: Boolean,
    castState: io.github.agimaulana.radio.core.radioplayer.RadioPlayerController.CastState,
    onCollapse: () -> Unit,
    onCastClick: () -> Unit,
    onMore: () -> Unit,
    progress: Float,
    dynamicTopPadding: androidx.compose.ui.unit.Dp,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = dynamicTopPadding)
            .alpha(progress)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = if (isBuffering) "BUFFERING" else "NOW PLAYING",
            modifier = Modifier.align(Alignment.CenterStart),
            style = MaterialTheme.typography.labelLarge,
            color = Color.White.copy(alpha = 0.7f),
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )

        Row(
            modifier = Modifier.align(Alignment.CenterEnd),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CastButton(
                castState = castState,
                onClick = onCastClick
            )

            IconButton(
                onClick = onCollapse,
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.White.copy(alpha = 0.1f), CircleShape)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_collapse),
                    contentDescription = "Collapse",
                    tint = Color.White,
                )
            }
        }

        if (isMoreMenuEnabled) {
            IconButton(
                onClick = onMore,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(40.dp)
                    .background(Color.White.copy(alpha = 0.1f), CircleShape)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_more),
                    contentDescription = "More",
                    tint = Color.White,
                )
            }
        }
    }
}

@Composable
private fun GenreChip(genre: String) {
    Box(
        modifier = Modifier
            .background(Color(0xFFFF69B4).copy(alpha = 0.2f), RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFFFF69B4).copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(
            text = genre.lowercase(),
            color = Color(0xFFFF69B4),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun RadioImage(
    progress: Float,
    stationName: String,
    imageUrl: String,
    modifier: Modifier = Modifier,
) {
    val imageScale = 0.8f + (0.2f * progress)
    Box(
        modifier = modifier
            .fillMaxWidth(0.8f)
            .aspectRatio(1f)
            .graphicsLayer {
                scaleX = imageScale
                scaleY = imageScale
                alpha = progress.coerceIn(0f, 1f)
            },
        contentAlignment = Alignment.Center
    ) {
        // Rings Background
        Image(
            painter = painterResource(id = R.drawable.ic_station_rings),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )

        if (imageUrl.isNotEmpty()) {
            Image(
                painter = rememberAsyncImagePainter(
                    model = imageUrl,
                ),
                contentDescription = stationName,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize(0.85f)
                    .shadow(24.dp, RoundedCornerShape(24.dp))
                    .clip(RoundedCornerShape(24.dp))
            )
        }
    }
}

@Composable
private fun PlayerControls(
    isPlaying: Boolean,
    isBuffering: Boolean,
    isFavoriteEnabled: Boolean,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (isBuffering) {
        Column(
            modifier = modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            BufferingIcon(
                modifier = Modifier.size(48.dp),
                tint = Color(0xFFFF69B4)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "CONNECTING TO STREAM",
                style = MaterialTheme.typography.labelLarge,
                color = Color.White.copy(alpha = 0.7f),
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
        }
    } else {
        Row(
            modifier = modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            IconButton(
                onClick = onStop,
                modifier = Modifier
                    .size(48.dp)
                    .background(Color.White.copy(alpha = 0.1f), CircleShape)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_circle_stop),
                    contentDescription = "Stop",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.size(32.dp))

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .shadow(16.dp, CircleShape, spotColor = Color(0xFFFF69B4))
                    .background(Color(0xFFFF69B4), CircleShape)
                    .clickable(
                        onClick = if (isPlaying) onPause else onPlay
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(
                        id = if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
                    ),
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.size(32.dp))

            if (isFavoriteEnabled) {
                IconButton(
                    onClick = { /* TODO: Favorite */ },
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.White.copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_favorite_border),
                        contentDescription = "Favorite",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            } else {
                Spacer(modifier = Modifier.size(48.dp))
            }
        }
    }
}

@Composable
private fun ActionRow(
    onStream: () -> Unit,
    onSleep: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ActionButton(
            iconRes = R.drawable.ic_stream,
            label = "STREAM",
            onClick = onStream
        )
        ActionButton(
            iconRes = R.drawable.ic_sleep,
            label = "SLEEP",
            onClick = onSleep
        )
        ActionButton(
            iconRes = R.drawable.ic_share,
            label = "SHARE",
            onClick = onShare
        )
    }
}

@Composable
private fun ActionButton(
    iconRes: Int,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(48.dp)
                .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = label,
                tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(24.dp)
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.5f),
            fontWeight = FontWeight.Bold
        )
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
                name = "Most 105.8 FM Jakarta",
                genre = "90s",
                imageUrl = "",
                streamUrl = "",
                isBuffering = false,
                isPlaying = true
            ),
            playerColors = PlayerColors(
                dominant = Color(0xFF1C1A24),
                vibrant = Color(0xFF3a1040),
                darkMuted = Color(0xFF0e0c14),
            ),
            featureFlag = StationListViewModel.UiState.FeatureFlag(),
            castState = io.github.agimaulana.radio.core.radioplayer.RadioPlayerController.CastState.NOT_CONNECTED,
            onPlay = {},
            onPause = {},
            onStop = {},
            onCollapse = {},
            onCastClick = {},
        )
    }
}

@Preview(
    showSystemUi = true,
    showBackground = true,
)
@Composable
private fun FullPlayerBufferingPreview() {
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
            playerColors = PlayerColors(
                dominant = Color(0xFF1C1A24),
                vibrant = Color(0xFF3a1040),
                darkMuted = Color(0xFF0e0c14),
            ),
            featureFlag = StationListViewModel.UiState.FeatureFlag(),
            castState = io.github.agimaulana.radio.core.radioplayer.RadioPlayerController.CastState.NOT_CONNECTED,
            onPlay = {},
            onPause = {},
            onStop = {},
            onCollapse = {},
            onCastClick = {},
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
                progress = 1f,
                station = Station(
                    serverUuid = "uuid",
                    name = "24.7 FM",
                    genre = "Pop",
                    imageUrl = "",
                    streamUrl = "",
                    isBuffering = false,
                    isPlaying = false
                ),
                playerColors = PlayerColors(
                    dominant = Color(0xFF1C1A24),
                    vibrant = Color(0xFF3a1040),
                    darkMuted = Color(0xFF0e0c14),
                ),
                featureFlag = StationListViewModel.UiState.FeatureFlag(),
                castState = io.github.agimaulana.radio.core.radioplayer.RadioPlayerController.CastState.NOT_CONNECTED,
                onPlay = {},
                onPause = {},
                onStop = {},
                onCollapse = {},
                onCastClick = {},
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
