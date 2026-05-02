package io.github.agimaulana.radio.feature.widget

import android.content.Context
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import dagger.hilt.android.EntryPointAccessors
import io.github.agimaulana.radio.core.design.glance.theme.RadioGlanceTheme
import io.github.agimaulana.radio.feature.widget.component.FullWidget
import io.github.agimaulana.radio.feature.widget.component.QuickTilesWidget
import io.github.agimaulana.radio.feature.widget.component.StationListWidget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import timber.log.Timber

class RadioWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Responsive(
        setOf(
            DpSize(110.dp, 110.dp), // 2x2
            DpSize(220.dp, 110.dp), // 4x2
            DpSize(220.dp, 220.dp)  // 4x4
        )
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entryPoint = EntryPointAccessors.fromApplication(context, WidgetEntryPoint::class.java)
        val getPinnedStationsUseCase = entryPoint.getPinnedStationsUseCase()
        val radioPlayerControllerFactory = entryPoint.radioPlayerControllerFactory()

        val stations = getPinnedStationsUseCase.execute().first()
        val (currentStation, isPlaying) = withContext(Dispatchers.Main) {
            val controller = try { radioPlayerControllerFactory.get() } catch (e: Exception) { null }
            val currentMediaId = controller?.currentMediaId
            val isPlaying = controller?.isPlaying ?: false
            val currentStation = if (currentMediaId != null) {
                stations.find { it.stationUuid == currentMediaId } ?: try {
                    entryPoint.getRadioStationUseCase().execute(currentMediaId)
                } catch (e: Exception) {
                    Timber.e(e, "Cannot get station $currentMediaId")
                    null
                }
            } else null
            currentStation to isPlaying
        }

        provideContent {
            val size = LocalSize.current

            RadioGlanceTheme {
                Box(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(GlanceTheme.colors.background)
                        .padding(8.dp)
                ) {
                    Box(modifier = GlanceModifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
                        Image(
                            provider = ImageProvider(io.github.agimaulana.radio.core.design.R.drawable.ic_toolbar_rings_bg),
                            contentDescription = null,
                            modifier = GlanceModifier.fillMaxWidth().height(110.dp)
                        )
                    }

                    when {
                        size.height >= 220.dp -> FullWidget(stations, currentStation, isPlaying)
                        size.width >= 220.dp -> StationListWidget(stations)
                        else -> QuickTilesWidget(stations)
                    }
                }
            }
        }
    }
}
