package io.github.agimaulana.radio.feature.widget.small

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionStartActivity
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
) {
    val openAppIntent = LocalContext.current.packageManager
        .getLaunchIntentForPackage(LocalContext.current.packageName)
        ?.apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
        ?: Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
            `package` = LocalContext.current.packageName
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

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

        Box(
            modifier = GlanceModifier
                .padding(top = 12.dp)
                .fillMaxWidth()
                .clickable(actionStartActivity(openAppIntent))
                .background(ColorProvider(RadioTheme.colors.primary))
                .padding(horizontal = 24.dp, vertical = 10.dp),
        ) {
            Column(
                modifier = GlanceModifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Open app",
                    style = TextStyle(
                        color = ColorProvider(RadioTheme.colors.primaryForeground),
                    ),
                )
            }
        }
    }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview
@Composable
private fun EmptyPinnedStationsPreview() {
    SmallWidgetEmptyPinnedStations()
}
