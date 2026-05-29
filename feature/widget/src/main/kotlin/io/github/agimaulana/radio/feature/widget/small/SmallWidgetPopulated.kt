package io.github.agimaulana.radio.feature.widget.small

import android.content.Intent
import android.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.ButtonColors
import androidx.glance.ButtonDefaults
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.components.FilledButton
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.preview.Preview
import androidx.glance.unit.ColorProvider
import io.github.agimaulana.radio.core.design.RadioTheme
import io.github.agimaulana.radio.feature.widget.PinnedTile
import io.github.agimaulana.radio.feature.widget.TileDispatchActivity
import io.github.agimaulana.radio.feature.widget.WidgetViewModel

@Composable
internal fun SmallWidgetPopulated(
    uiState: WidgetViewModel.UiState,
    modifier: GlanceModifier = GlanceModifier,
) {
    val tiles = uiState.pinnedStationDetails
    Column(modifier = modifier.padding(horizontal = 8.dp)) {
        for (row in 0..1) {
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                for (col in 0..1) {
                    val index = row * 2 + col
                    val tile = tiles.getOrNull(index)
                    if (tile != null) {
                        val intent = Intent(
                            LocalContext.current,
                            TileDispatchActivity::class.java,
                        ).apply {
                            putExtra("extra_media_id", tile.mediaId)
                            addFlags(
                                Intent.FLAG_ACTIVITY_NEW_TASK or
                                    Intent.FLAG_ACTIVITY_SINGLE_TOP,
                            )
                        }
                        TileView(
                            tile = tile,
                            modifier = GlanceModifier.fillMaxWidth(),
                            onClickIntent = intent,
                        )
                    } else {
                        TileViewEmpty(modifier = GlanceModifier.fillMaxWidth())
                    }
                }
            }
        }
    }
}

@Composable
private fun TileView(
    tile: PinnedTile,
    modifier: GlanceModifier = GlanceModifier,
    onClickIntent: Intent,
) {
    val label = "${tile.shortName}\n${tile.name} ${tile.frequency}".trimIndent()
    FilledButton(
        text = label,
        onClick = actionStartActivity(onClickIntent),
        modifier = modifier.padding(4.dp),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = ColorProvider(RadioTheme.colors.primary),
            contentColor = ColorProvider(RadioTheme.colors.primaryForeground),
        ),
    )
}

@Composable
private fun TileViewEmpty(modifier: GlanceModifier = GlanceModifier) {
    Column(
        modifier = modifier.padding(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
    }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview
@Composable
private fun SmallWidgetPopulatedPreview() {
    SmallWidgetPopulated(
        uiState = WidgetViewModel.UiState(
            pinnedStationDetails = listOf(
                PinnedTile("id1", "BBC Radio 1", "98.8 FM", "BBC", Color.parseColor("#BC1A29")),
                PinnedTile("id2", "NPR News", "88.5 FM", "NPR", Color.parseColor("#1A7BB4")),
                PinnedTile("id3", "Jazz FM", "102.2 FM", "JAZZ", Color.parseColor("#2D5F2D")),
                PinnedTile("id4", "KEXP 90.3", "90.3 FM", "KEXP", Color.parseColor("#6B2FA0")),
            ),
        ),
    )
}
