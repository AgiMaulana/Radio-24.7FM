package io.github.agimaulana.radio.feature.widget.component

import androidx.compose.runtime.Composable
import androidx.glance.GlanceModifier
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import io.github.agimaulana.radio.domain.api.entity.RadioStation
import androidx.compose.ui.tooling.preview.Preview
import androidx.glance.GlanceComposable

@Composable
@GlanceComposable
fun StationListWidget(
    stations: List<RadioStation>,
    modifier: GlanceModifier = GlanceModifier,
) {
    if (stations.isEmpty()) {
        EmptyState(modifier = modifier)
    } else {
        Column(modifier = modifier.fillMaxSize()) {
            HeaderSection()
            stations.take(2).forEach { station ->
                StationRow(station)
            }
        }
    }
}

@Preview(widthDp = 220, heightDp = 110)
@Composable
fun StationListWidgetPreview() {
    GlancePreview {
        StationListWidget(
            stations = listOf(
                RadioStation("1", "Most 105.8 FM Jakarta", listOf("90s"), "", "", ""),
                RadioStation("2", "Gen 98.7 FM Jakarta", listOf("local"), "", "", "")
            )
        )
    }
}
