package io.github.agimaulana.radio.feature.widget.small

import android.content.Intent
import android.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ExperimentalGlanceApi
import androidx.glance.GlanceModifier
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionSendBroadcast
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.GridCells
import androidx.glance.appwidget.lazy.LazyVerticalGrid
import androidx.glance.appwidget.lazy.items
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.preview.Preview
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import io.github.agimaulana.radio.core.design.RadioTheme
import io.github.agimaulana.radio.feature.widget.PinnedTile
import io.github.agimaulana.radio.feature.widget.PlayPinnedStationReceiver
import io.github.agimaulana.radio.feature.widget.WidgetViewModel
import io.github.agimaulana.radio.feature.widget.component.GlancePlayingWaveIndicator

@Suppress("FunctionNaming")
@OptIn(ExperimentalGlanceApi::class)
@Composable
internal fun SmallWidgetPopulated(
    tiles: List<PinnedTile>,
    modifier: GlanceModifier = GlanceModifier,
    tileToIntent: (PinnedTile) -> Intent,
) {
    LazyVerticalGrid(
        gridCells = GridCells.Fixed(2),
        modifier = modifier.padding(horizontal = 4.dp).fillMaxSize(),
    ) {
        items(tiles) { tile ->
            TileView(
                tile = tile,
                modifier = GlanceModifier.fillMaxWidth(),
                onClickIntent = tileToIntent(tile),
            )
        }
    }
}

@Suppress("FunctionNaming")
@Composable
private fun TileView(
    tile: PinnedTile,
    modifier: GlanceModifier = GlanceModifier,
    onClickIntent: Intent,
) {
    Column(
        modifier = modifier
            .padding(8.dp)
            .background(ColorProvider(RadioTheme.colors.card))
            .cornerRadius(16.dp)
            .clickable(actionSendBroadcast(onClickIntent)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(modifier = GlanceModifier.height(12.dp))
        Box(
            modifier = GlanceModifier
                .size(60.dp)
                .background(ColorProvider(ComposeColor(tile.brandColor)))
                .cornerRadius(12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = tile.shortName,
                style = TextStyle(
                    color = ColorProvider(ComposeColor.White),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                ),
            )
            
            if (tile.isPlaying) {
                Box(
                    modifier = GlanceModifier.fillMaxSize()
                        .background(ColorProvider(ComposeColor(0x88000000)))
                        .cornerRadius(12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = GlanceModifier.fillMaxSize().padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        GlancePlayingWaveIndicator(
                            height = 12.dp,
                            barWidth = 2.dp,
                            barSpacing = 1.dp
                        )
                    }
                }
            }
        }
        Spacer(modifier = GlanceModifier.height(8.dp))
        Text(
            text = "${tile.name} ${tile.frequency}",
            modifier = GlanceModifier.padding(horizontal = 8.dp),
            style = TextStyle(
                color = ColorProvider(RadioTheme.colors.foreground),
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                fontWeight = if (tile.isPlaying) FontWeight.Bold else FontWeight.Normal
            ),
            maxLines = 1,
        )
        Spacer(modifier = GlanceModifier.height(12.dp))
    }
}

@Suppress("UnusedPrivateMember", "FunctionNaming")
@OptIn(ExperimentalGlancePreviewApi::class)
@Preview
@Composable
private fun SmallWidgetPopulatedPreview() {
    val tiles = listOf(
        PinnedTile("id1", "Most", "105.8", "MOST", Color.parseColor("#438C76"), isPlaying = true),
        PinnedTile("id2", "Gen", "98.7", "GEN", Color.parseColor("#634EB8")),
        PinnedTile("id3", "Jak FM", "", "JAK", Color.parseColor("#3871C1")),
        PinnedTile("id4", "Insania FM", "", "INS", Color.parseColor("#B34423")),
    )
    SmallWidgetPopulated(
        tiles = tiles,
        tileToIntent = { Intent() },
    )
}
