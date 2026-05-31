package io.github.agimaulana.radio

import android.app.Application
import android.os.Build
import androidx.glance.appwidget.GlanceAppWidgetManager
import dagger.hilt.android.HiltAndroidApp
import io.github.agimaulana.radio.feature.widget.RadioWidgetReceiver
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

@HiltAndroidApp
class Radio247FmApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            MainScope().launch {
                GlanceAppWidgetManager(this@Radio247FmApplication)
                    .setWidgetPreviews(RadioWidgetReceiver::class)
            }
        }
    }
}
