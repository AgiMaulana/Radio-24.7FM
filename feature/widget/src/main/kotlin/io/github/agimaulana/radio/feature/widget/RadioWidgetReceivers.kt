package io.github.agimaulana.radio.feature.widget

import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.updateAll
import io.github.agimaulana.radio.core.common.WidgetConstants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

abstract class BaseWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = RadioWidget()

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == WidgetConstants.ACTION_REFRESH_WIDGETS) {
            CoroutineScope(Dispatchers.Main).launch {
                glanceAppWidget.updateAll(context)
            }
        }
    }
}

class TilesWidgetReceiver : BaseWidgetReceiver()

class ListWidgetReceiver : BaseWidgetReceiver()

class FullWidgetReceiver : BaseWidgetReceiver()
