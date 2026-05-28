package io.github.agimaulana.radio.feature.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.ImageProvider
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import io.github.agimaulana.radio.feature.widget.small.SmallWidget

class RadioWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            SmallWidget(
                titleIcon = ImageProvider(R.drawable.ic_star_filled),
            )
        }
    }
}
