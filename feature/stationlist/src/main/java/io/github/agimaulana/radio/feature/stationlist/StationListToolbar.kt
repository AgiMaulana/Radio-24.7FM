package io.github.agimaulana.radio.feature.stationlist

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import io.github.agimaulana.radio.core.design.RadioTheme
import io.github.agimaulana.radio.core.design.theme.PreviewTheme
import io.github.agimaulana.radio.core.radioplayer.RadioPlayerController.CastState
import io.github.agimaulana.radio.feature.stationlist.StationListViewModel.UiState
import io.github.agimaulana.radio.feature.stationlist.StationListViewModel.UiState.Station
import io.github.agimaulana.radio.feature.stationlist.component.CastButton
import kotlinx.collections.immutable.persistentListOf

private val HeroBackgroundColor = Color(0xFF1C1A24)
private const val DarkScrimAlpha = 0.50f
private const val LightScrimAlpha = 0.82f
private const val EXPANDED_CONTENT_THRESHOLD = 0.2f
private val LocationOffsetExpanded = 278.dp

@Composable
internal fun StationListToolbar(
    dims: ToolbarDimensions,
    uiState: UiState,
    onSearch: (String) -> Unit,
    onCastClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentHeight = lerp(dims.expandedHeight, dims.collapsedHeight, dims.progress)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(currentHeight)
            .background(HeroBackgroundColor)
    ) {
        ToolbarBackground(dims.expandedHeight, dims.progress)

        val isLight = luminance(MaterialTheme.colorScheme.background) > 0.5f
        val scrimAlpha = if (isLight) LightScrimAlpha else DarkScrimAlpha

        ToolbarScrim(scrimAlpha)
        ToolbarContent(
            progress = dims.progress,
            uiState = uiState,
            onSearch = onSearch,
            onCastClick = onCastClick
        )
    }
}

private fun luminance(color: Color): Float =
    0.2126f * color.red + 0.7152f * color.green + 0.0722f * color.blue

@Composable
private fun ToolbarBackground(expandedHeight: Dp, progress: Float) {
    Image(
        painter = painterResource(id = R.drawable.collapsing_toolbar_background),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        alignment = Alignment.BottomCenter,
        modifier = Modifier
            .fillMaxWidth()
            .height(expandedHeight)
            .graphicsLayer { alpha = 1f - progress * 0.2f }
    )
}

@Composable
private fun ToolbarScrim(scrimAlpha: Float) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colorStops = arrayOf(
                        0.00f to Color.Black.copy(alpha = 0f),
                        0.50f to Color.Black.copy(alpha = scrimAlpha * 0.6f),
                        1.00f to Color.Black.copy(alpha = scrimAlpha * 0.85f),
                    )
                )
            )
    )
}

@Composable
private fun ToolbarContent(
    progress: Float,
    uiState: UiState,
    onSearch: (String) -> Unit,
    onCastClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        val titleSize = (32 * (1 - progress) + 20 * progress).sp

        Text(
            text = "24.7 FM",
            style = RadioTheme.typography.stationTitle.copy(
                fontSize = titleSize,
                fontWeight = FontWeight.Bold,
                color = Color.White
            ),
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(y = lerp(155.dp, 20.dp, progress))
        )

//        if (uiState.castState != CastState.NO_DEVICES) {
            CastButton(
                castState = uiState.castState,
                onClick = onCastClick,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(y = 8.dp)
            )
//        }

        CustomSearchBar(
            value = uiState.filterStationName.orEmpty(),
            onValueChange = onSearch,
            placeholder = if (progress < 0.5f) "Search your favourite station..." else "Search...",
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(
                    start = lerp(0.dp, 95.dp, progress),
                    end = if (uiState.castState != CastState.NO_DEVICES) {
                        lerp(0.dp, 48.dp, progress)
                    } else {
                        0.dp
                    }
                )
                .offset(y = lerp(205.dp, 8.dp, progress))
                .fillMaxWidth()
        )

        if (progress < EXPANDED_CONTENT_THRESHOLD && uiState.locationName != null) {
            LocationLabel(
                locationName = uiState.locationName,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(
                        y = lerp(
                            LocationOffsetExpanded,
                            220.dp,
                            progress
                        )
                    )
                    .graphicsLayer { alpha = (1f - progress * 5f).coerceAtLeast(0f) }
            )
        }
    }
}

@Composable
private fun LocationLabel(
    locationName: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(RadioTheme.colors.primary)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = locationName,
            color = RadioTheme.colors.primary,
            style = RadioTheme.typography.stationTitle.copy(
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.basicMarquee(
                iterations = Int.MAX_VALUE
            )
        )
    }
}

@Composable
private fun CustomSearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.1f))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_search),
                contentDescription = null,
                tint = RadioTheme.colors.primary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(modifier = Modifier.weight(1f)) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        color = Color.White.copy(alpha = 0.5f),
                        style = RadioTheme.typography.stationTitle.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal
                        )
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    textStyle = RadioTheme.typography.stationTitle.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.White
                    ),
                    cursorBrush = SolidColor(RadioTheme.colors.primary),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFF111111)
@Composable
private fun StationListToolbarPreview() {
    PreviewTheme {
        StationListToolbar(
            dims = ToolbarDimensions(
                expandedHeight = 404.dp,
                collapsedHeight = 88.dp,
                progress = 0f
            ),
            uiState = UiState(
                filterStationName = "",
                locationName = "Jakarta, Indonesia",
                pinnedStations = persistentListOf(
                    Station(
                        serverUuid = "1",
                        name = "Most 105.8 FM Jakarta",
                        genre = "90s",
                        imageUrl = "",
                        streamUrl = "",
                        isBuffering = false,
                        isPlaying = false,
                        isPinned = true
                    ),
                    Station(
                        serverUuid = "2",
                        name = "Prambors",
                        genre = "Top 40",
                        imageUrl = "",
                        streamUrl = "",
                        isBuffering = false,
                        isPlaying = false,
                        isPinned = true
                    )
                )
            ),
            onSearch = {},
            onCastClick = {}
        )
    }
}
