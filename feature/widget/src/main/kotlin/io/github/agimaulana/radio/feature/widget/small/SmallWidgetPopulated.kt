package io.github.agimaulana.radio.feature.widget.small

import android.content.Intent
import android.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ExperimentalGlanceApi
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
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

@Suppress("FunctionNaming")
@OptIn(ExperimentalGlanceApi::class)
@Composable
internal fun SmallWidgetPopulated(
    uiState: WidgetViewModel.UiState,
    modifier: GlanceModifier = GlanceModifier,
) {
    val tiles = uiState.pinnedStationDetails
    val context = LocalContext.current
    LazyVerticalGrid(
        gridCells = GridCells.Fixed(2),
        modifier = modifier.padding(8.dp).fillMaxSize(),
    ) {
        items(tiles) { tile ->
            val intent = Intent(context, PlayPinnedStationReceiver::class.java).apply {
                action = "io.github.agimaulana.radio.action.PLAY_PINNED"
                putExtra("extra_media_id", tile.mediaId)
                `package` = context.packageName
            }
            TileView(
                tile = tile,
                modifier = GlanceModifier.fillMaxWidth(),
                onClickIntent = intent,
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
            .padding(4.dp)
            .background(ColorProvider(RadioTheme.colors.card))
            .cornerRadius(16.dp)
            .clickable(actionSendBroadcast(onClickIntent)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(modifier = GlanceModifier.height(8.dp))
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
        }
        Spacer(modifier = GlanceModifier.height(8.dp))
        Text(
            text = "${tile.name} ${tile.frequency}",
            modifier = GlanceModifier.padding(horizontal = 8.dp),
            style = TextStyle(
                color = ColorProvider(RadioTheme.colors.foreground),
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
            ),
            maxLines = 1,
        )
        Spacer(modifier = GlanceModifier.height(8.dp))
    }
}

@Suppress("UnusedPrivateMember")
@OptIn(ExperimentalGlancePreviewApi::class)
@Preview
@Composable
private fun SmallWidgetPopulatedPreview() {
    SmallWidgetPopulated(
        uiState = WidgetViewModel.UiState(
            pinnedStationDetails = listOf(
                PinnedTile("id1", "Most", "105.8", "MOST", Color.parseColor("#438C76")),
                PinnedTile("id2", "Gen", "98.7", "GEN", Color.parseColor("#634EB8")),
                PinnedTile("id3", "Jak FM", "", "JAK", Color.parseColor("#3871C1")),
                PinnedTile("id4", "Insania FM", "", "INS", Color.parseColor("#B34423")),
            ),
        ),
    )
}
