package io.github.agimaulana.radio.feature.widget.small

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.Button
import androidx.glance.ButtonDefaults
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.Action
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.preview.Preview
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import io.github.agimaulana.radio.core.design.RadioTheme
import io.github.agimaulana.radio.feature.widget.R

@Composable
internal fun SmallWidgetEmptyPinnedStations(
    modifier: GlanceModifier = GlanceModifier,
    onOpenAppAction: Action? = null,
) {
    Column(
        modifier = modifier.padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = GlanceModifier
                .background(ImageProvider(R.drawable.pinned_icon_background))
                .padding(12.dp)
        ) {
            Image(
                provider = ImageProvider(R.drawable.ic_star_filled),
                contentDescription = null,
                colorFilter = ColorFilter.tint(ColorProvider(RadioTheme.colors.primary))
            )
        }

        Text(
            text = "No pins yet",
            modifier = GlanceModifier.padding(top = 8.dp),
            style = TextStyle(
                color = ColorProvider(RadioTheme.colors.foreground),
            ),
        )

        Text(
            text = "Long press the station to pin it",
            modifier = GlanceModifier.padding(top = 4.dp),
            style = TextStyle(
                color = ColorProvider(RadioTheme.colors.foreground.copy(alpha = 0.6f)),
            ),
        )

        if (onOpenAppAction != null) {
            Button(
                text = "Open app",
                onClick = onOpenAppAction,
                modifier = GlanceModifier
                    .padding(top = 12.dp)
                    .fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = ColorProvider(RadioTheme.colors.primary),
                    contentColor = ColorProvider(RadioTheme.colors.primaryForeground),
                ),
            )
        }
    }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview
@Composable
private fun EmptyPinnedStationsPreview() {
    SmallWidgetEmptyPinnedStations()
}
