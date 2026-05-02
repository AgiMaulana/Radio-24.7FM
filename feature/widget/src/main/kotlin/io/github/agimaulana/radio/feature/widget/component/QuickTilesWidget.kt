package io.github.agimaulana.radio.feature.widget.component

import androidx.compose.runtime.Composable
import androidx.glance.GlanceModifier
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.compose.ui.tooling.preview.Preview
import androidx.glance.GlanceComposable
import io.github.agimaulana.radio.domain.api.entity.RadioStation

@Composable
@GlanceComposable
fun QuickTilesWidget(
    stations: List<RadioStation>,
    modifier: GlanceModifier = GlanceModifier,
) {
    if (stations.isEmpty()) {
        EmptyState(modifier = modifier)
    } else {
        Column(
            modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val gridStations = stations.take(4)
            for (i in 0 until 2) {
                Row(modifier = GlanceModifier.fillMaxWidth().defaultWeight()) {
                    for (j in 0 until 2) {
                        val index = i * 2 + j
                        if (index < gridStations.size) {
                            StationTile(gridStations[index], modifier = GlanceModifier.defaultWeight())
                        } else {
                            Spacer(modifier = GlanceModifier.defaultWeight())
                        }
                    }
                }
            }
        }
    }
}

@Preview(widthDp = 110, heightDp = 110)
@Composable
fun QuickTilesWidgetPreview() {
    GlancePreview {
        QuickTilesWidget(
            stations = listOf(
                RadioStation("1", "Most 105.8", listOf("90s"), "", "", ""),
                RadioStation("2", "Gen 98.7", listOf("local"), "", "", ""),
                RadioStation("3", "Jak FM", listOf("pop"), "", "", ""),
                RadioStation("4", "Insania", listOf("rock"), "", "", "")
            )
        )
    }
}
