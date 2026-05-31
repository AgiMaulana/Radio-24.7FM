package io.github.agimaulana.radio.feature.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import io.github.agimaulana.radio.core.glance.state.WidgetStateManager
import io.github.agimaulana.radio.core.glance.viewmodel.hilt.hiltGlanceViewModel
import io.github.agimaulana.radio.feature.widget.small.SmallWidget
import kotlinx.coroutines.Job
import androidx.compose.runtime.collectAsState

class RadioWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val vm by hiltGlanceViewModel<WidgetViewModel>(id, context)
        vm.init()

        if (observerJob == null) {
            observerJob = WidgetStateManager.observe(
                context = context,
                widgetClass = RadioWidget::class.java,
                stateFlow = vm.uiState,
                debounceMillis = 200L,
            ) { _ctx, glanceId, _ ->
                RadioWidget().update(_ctx, glanceId)
            }
        }

        provideContent {
            val uiState = vm.uiState.collectAsState().value
            RadioGlanceTheme {
                SmallWidget(uiState = uiState)
            }
        }
    }

    companion object {
        private var observerJob: Job? = null
    }
}
