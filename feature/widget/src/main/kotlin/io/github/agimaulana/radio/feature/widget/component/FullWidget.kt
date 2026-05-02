package io.github.agimaulana.radio.feature.widget.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import io.github.agimaulana.radio.domain.api.entity.RadioStation
import androidx.compose.ui.tooling.preview.Preview
import androidx.glance.GlanceComposable
import io.github.agimaulana.radio.core.design.R
import io.github.agimaulana.radio.feature.widget.TogglePlaybackAction

@Composable
@GlanceComposable
fun FullWidget(
    stations: List<RadioStation>,
    currentStation: RadioStation?,
    isPlaying: Boolean,
    modifier: GlanceModifier = GlanceModifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .padding(8.dp)
                .background(GlanceTheme.colors.secondaryContainer),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = GlanceModifier.size(56.dp).background(GlanceTheme.colors.primary),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    provider = ImageProvider(R.drawable.ic_station_rings),
                    contentDescription = null,
                    modifier = GlanceModifier.fillMaxSize()
                )
            }
            Spacer(modifier = GlanceModifier.width(8.dp))
            Column(modifier = GlanceModifier.defaultWeight()) {
                Text(
                    text = currentStation?.name ?: "Nothing playing",
                    style = TextStyle(color = GlanceTheme.colors.onSecondaryContainer, fontWeight = FontWeight.Bold),
                    maxLines = 1
                )
                Text(
                    text = currentStation?.tags?.firstOrNull() ?: "Tap a station below to start",
                    style = TextStyle(color = GlanceTheme.colors.onSecondaryContainer, fontSize = 12.sp),
                    maxLines = 1
                )
            }
            if (currentStation != null) {
                Image(
                    provider = ImageProvider(if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play),
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    modifier = GlanceModifier
                        .size(32.dp)
                        .clickable(actionRunCallback<TogglePlaybackAction>())
                )
            }
        }
        
        Spacer(modifier = GlanceModifier.height(8.dp))
        Text(
            text = "★ PINNED",
            style = TextStyle(color = GlanceTheme.colors.primary, fontWeight = FontWeight.Bold, fontSize = 12.sp),
            modifier = GlanceModifier.padding(start = 8.dp)
        )
        
        stations.take(3).forEach { station ->
            StationRow(station)
        }
    }
}

@Preview(widthDp = 220, heightDp = 220)
@Composable
fun FullWidgetPlayingPreview() {
    GlancePreview {
        val station = RadioStation("1", "Most 105.8 FM Jakarta", listOf("90s"), "", "", "")
        FullWidget(
            stations = listOf(station),
            currentStation = station,
            isPlaying = true
        )
    }
}

@Preview(widthDp = 220, heightDp = 220)
@Composable
fun FullWidgetIdlePreview() {
    GlancePreview {
        FullWidget(
            stations = listOf(
                RadioStation("1", "Most 105.8", listOf("90s"), "", "", ""),
                RadioStation("2", "Gen 98.7", listOf("local"), "", "", "")
            ),
            currentStation = null,
            isPlaying = false
        )
    }
}
