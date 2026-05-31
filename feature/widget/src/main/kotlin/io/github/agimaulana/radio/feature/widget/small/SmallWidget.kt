package io.github.agimaulana.radio.feature.widget.small

import android.content.Intent
import android.content.pm.PackageManager
import androidx.compose.runtime.Composable
import androidx.glance.GlanceModifier
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.Action
import androidx.glance.appwidget.action.actionSendBroadcast
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.components.Scaffold
import androidx.glance.appwidget.components.TitleBar
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.preview.Preview
import androidx.glance.unit.ColorProvider
import io.github.agimaulana.radio.core.design.RadioTheme
import io.github.agimaulana.radio.feature.widget.PinnedTile
import io.github.agimaulana.radio.feature.widget.PlayPinnedStationReceiver
import io.github.agimaulana.radio.feature.widget.PlayPinnedStationReceiver.Companion.ACTION_PAUSE_PINNED
import io.github.agimaulana.radio.feature.widget.PlayPinnedStationReceiver.Companion.ACTION_PLAY_PINNED
import io.github.agimaulana.radio.feature.widget.R
import io.github.agimaulana.radio.feature.widget.WidgetViewModel

@Composable
internal fun SmallWidget(
    uiState: WidgetViewModel.UiState,
    modifier: GlanceModifier = GlanceModifier,
) {
    val context = LocalContext.current
    SmallWidgetContent(
        uiState = uiState,
        modifier = modifier,
        emptyStateClickAction = createOnEmptyStateClickAction(
            packageManager = context.packageManager,
            packageName = context.packageName,
        ),
        tileToAction = { tile ->
            val intent = Intent(context, PlayPinnedStationReceiver::class.java).apply {
                action = if (tile.isPlaying) ACTION_PAUSE_PINNED else ACTION_PLAY_PINNED
                putExtra("extra_media_id", tile.mediaId)
                `package` = context.packageName
            }
            actionSendBroadcast(intent)
        },
    )
}

@Composable
private fun SmallWidgetContent(
    uiState: WidgetViewModel.UiState,
    modifier: GlanceModifier = GlanceModifier,
    emptyStateClickAction: Action? = null,
    tileToAction: (PinnedTile) -> Action,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        backgroundColor = ColorProvider(RadioTheme.colors.background),
        titleBar = {
            TitleBar(
                startIcon = ImageProvider(R.drawable.ic_app),
                title = "24.7 FM",
                iconColor = null,
                textColor = ColorProvider(RadioTheme.colors.foreground),
            )
        },
    ) {
        if (uiState.pinnedStationDetails.isEmpty()) {
            SmallWidgetEmptyPinnedStations(
                modifier = GlanceModifier.fillMaxWidth(),
                onOpenAppAction = emptyStateClickAction,
            )
        } else {
            SmallWidgetPopulated(
                tiles = uiState.pinnedStationDetails,
                modifier = GlanceModifier.fillMaxWidth(),
                tileToAction = tileToAction,
            )
        }
    }
}

private fun createOnEmptyStateClickAction(
    packageManager: PackageManager,
    packageName: String,
): Action {
    return actionStartActivity(
        packageManager.getLaunchIntentForPackage(packageName)
            ?.apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            } ?: Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
            `package` = packageName
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
    )
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview
@Composable
private fun SmallWidgetContentPreview() {
    SmallWidgetContent(
        uiState = WidgetViewModel.UiState(),
        tileToAction = { actionSendBroadcast(Intent()) },
    )
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview
@Composable
private fun SmallWidgetContentPopulatedPreview() {
    SmallWidgetContent(
        uiState = WidgetViewModel.UiState(
            pinnedStationDetails = listOf(
                PinnedTile("id1", "BBC Radio 1", "98.8 FM", "BBC", android.graphics.Color.parseColor("#BC1A29")),
                PinnedTile("id2", "NPR News", "88.5 FM", "NPR", android.graphics.Color.parseColor("#1A7BB4")),
                PinnedTile("id3", "Jazz FM", "102.2 FM", "JAZZ", android.graphics.Color.parseColor("#2D5F2D")),
                PinnedTile("id4", "KEXP 90.3", "90.3 FM", "KEXP", android.graphics.Color.parseColor("#6B2FA0")),
            ),
        ),
        tileToAction = { actionSendBroadcast(Intent()) },
    )
}
