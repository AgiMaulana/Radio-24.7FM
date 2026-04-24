package io.github.agimaulana.radio

import android.content.Context
import androidx.startup.Initializer
import io.sentry.Sentry
import io.sentry.SentryLevel
import io.sentry.android.core.SentryAndroid
import io.sentry.android.timber.SentryTimberTree
import timber.log.Timber

class SentryInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        if (BuildConfig.SENTRY_DSN.isBlank()) {
            return
        }

        SentryAndroid.init(context) { options ->
            options.dsn = BuildConfig.SENTRY_DSN
            options.isEnableAutoSessionTracking = true
            options.environment = if (!BuildConfig.DEBUG) "production" else "development"
            options.metrics.isEnabled = true
        }

        Timber.plant(
            SentryTimberTree(
                Sentry.getCurrentScopes(),
                SentryLevel.ERROR,
                SentryLevel.DEBUG,
            )
        )
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = listOf(
        LoggingInitializer::class.java,
    )
}
