package io.github.agimaulana.radio.feature.widget.component

import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
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
fun StationRow(station: RadioStation, modifier: GlanceModifier = GlanceModifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(GlanceTheme.colors.surface)
            .padding(8.dp)
            .clickable(actionStartActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse("radio247fm://stations"))
            )),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = GlanceModifier
                .size(40.dp)
                .background(GlanceTheme.colors.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = station.name.take(1),
                style = TextStyle(color = GlanceTheme.colors.onPrimaryContainer, fontWeight = FontWeight.Bold)
            )
        }
        Spacer(modifier = GlanceModifier.width(8.dp))
        Column(modifier = GlanceModifier.defaultWeight()) {
            Text(
                text = station.name,
                style = TextStyle(color = GlanceTheme.colors.onSurface, fontWeight = FontWeight.Bold),
                maxLines = 1
            )
            Text(
                text = station.tags.firstOrNull() ?: "",
                style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontSize = 12.sp),
                maxLines = 1
            )
        }
        Image(
            provider = ImageProvider(android.R.drawable.ic_media_play),
            contentDescription = "Play",
            modifier = GlanceModifier
                .size(32.dp)
                .clickable(actionRunCallback<PlayStationAction>(
                    actionParametersOf(StationUuidKey to station.stationUuid)
                ))
        )
    }
}

@Preview(widthDp = 220, heightDp = 60)
@Composable
fun StationRowPreview() {
    RadioGlanceTheme {
        StationRow(
            station = RadioStation("1", "Most 105.8 FM Jakarta", listOf("90s"), "", "", "")
        )
    }
}
