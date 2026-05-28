package io.github.agimaulana.radio.feature.widget.small

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.glance.GlanceModifier
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.components.Scaffold
import androidx.glance.appwidget.components.TitleBar
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.preview.Preview
import androidx.glance.unit.ColorProvider
import io.github.agimaulana.radio.core.design.RadioTheme
import io.github.agimaulana.radio.feature.widget.R

@Composable
internal fun SmallWidget(
    modifier: GlanceModifier = GlanceModifier,
    titleIcon: ImageProvider = ImageProvider(R.drawable.ic_star_filled),
) {
    val openAppAction = actionStartActivity(
        LocalContext.current.packageManager
            .getLaunchIntentForPackage(LocalContext.current.packageName)
            ?.apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
            ?: Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
                `package` = LocalContext.current.packageName
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        backgroundColor = ColorProvider(RadioTheme.colors.background),
        titleBar = {
            TitleBar(
                startIcon = titleIcon,
                title = "247FM",
            )
        },
    ) {
        SmallWidgetEmptyPinnedStations(
            modifier = GlanceModifier.fillMaxWidth(),
            onOpenAppAction = openAppAction,
        )
    }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview
@Composable
private fun SmallWidgetPreview() {
    SmallWidget()
}
