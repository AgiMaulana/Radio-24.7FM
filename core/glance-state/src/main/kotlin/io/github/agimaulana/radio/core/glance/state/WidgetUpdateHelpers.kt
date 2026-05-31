package io.github.agimaulana.radio.core.glance.state

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.GlanceId

/**
 * Convenience helpers for updating Glance widgets.
 */
public suspend fun <T> updateAllWidgets(
    context: Context,
    widgetClass: Class<out GlanceAppWidget>,
    latest: T,
    updater: suspend (Context, GlanceId, T) -> Unit
) {
    val manager = GlanceAppWidgetManager(context)
    val glanceIds = manager.getGlanceIds(widgetClass)
    for (id in glanceIds) {
        updater(context, id, latest)
    }
}
