package io.github.agimaulana.radio.initializer

import android.content.Context
import androidx.startup.Initializer
import io.github.agimaulana.radio.BuildConfig
import timber.log.Timber

class LoggingInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}
