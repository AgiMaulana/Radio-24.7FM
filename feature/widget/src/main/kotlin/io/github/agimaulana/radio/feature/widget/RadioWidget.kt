package io.github.agimaulana.radio.feature.widget

import android.content.Context
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.PreviewSizeMode
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import io.github.agimaulana.radio.core.glance.state.WidgetStateManager
import io.github.agimaulana.radio.core.glance.viewmodel.hilt.hiltGlanceViewModel
import io.github.agimaulana.radio.feature.widget.small.SmallWidget
import io.github.agimaulana.radio.feature.widget.small.SmallWidgetContentPreview

class RadioWidget : GlanceAppWidget() {

    override val previewSizeMode: PreviewSizeMode = SizeMode.Responsive(
        setOf(DpSize(250.dp, 120.dp)),
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val vm by hiltGlanceViewModel<WidgetViewModel>(id, context)
        vm.init()

        WidgetStateManager.observe(
            context = context,
            widgetClass = RadioWidget::class.java,
            stateFlow = vm.uiState,
            debounceMillis = 200L,
        ) { _ctx, glanceId, _ ->
            RadioWidget().update(_ctx, glanceId)
        }

        provideContent {
            val uiState = vm.uiState.collectAsState().value
            RadioGlanceTheme {
                SmallWidget(uiState = uiState)
            }
        }
    }

    override suspend fun providePreview(context: Context, widgetCategory: Int) {
        provideContent {
            SmallWidgetContentPreview()
        }
    }

}
