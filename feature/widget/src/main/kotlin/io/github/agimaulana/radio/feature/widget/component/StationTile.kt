package io.github.agimaulana.radio.feature.widget.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import io.github.agimaulana.radio.domain.api.entity.RadioStation
import io.github.agimaulana.radio.feature.widget.PlayStationAction
import io.github.agimaulana.radio.feature.widget.StationUuidKey
import androidx.compose.ui.tooling.preview.Preview
import androidx.glance.GlanceComposable
import io.github.agimaulana.radio.core.design.glance.theme.RadioGlanceTheme

@Composable
@GlanceComposable
fun StationTile(station: RadioStation, modifier: GlanceModifier = GlanceModifier) {
    Box(
        modifier = modifier
            .padding(4.dp)
            .clickable(actionRunCallback<PlayStationAction>(
                actionParametersOf(StationUuidKey to station.stationUuid)
            )),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = GlanceModifier
                    .size(48.dp)
                    .background(GlanceTheme.colors.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = station.name.take(1).uppercase(),
                    style = TextStyle(
                        color = GlanceTheme.colors.onPrimaryContainer,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                )
            }
            Text(
                text = station.name,
                maxLines = 1,
                style = TextStyle(
                    color = GlanceTheme.colors.onBackground,
                    fontSize = 10.sp
                ),
                modifier = GlanceModifier.padding(top = 2.dp)
            )
        }
    }
}

@Preview(widthDp = 60, heightDp = 80)
@Composable
fun StationTilePreview() {
    RadioGlanceTheme {
        StationTile(
            station = RadioStation("1", "Most 105.8", listOf("90s"), "", "", "")
        )
    }
}
