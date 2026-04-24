package io.github.agimaulana.radio.core.tracker.internal

import io.github.agimaulana.radio.core.tracker.EventTracker
import io.sentry.Sentry
import io.sentry.metrics.SentryMetricsParameters
import javax.inject.Inject

internal class SentryEventTracker @Inject constructor() : EventTracker {
    override fun track(
        eventName: String,
        properties: Map<String, String>,
    ) {
        val attributes = mutableMapOf<String, Any>(
            "event_name" to eventName,
        ).apply {
            putAll(properties)
        }

        Sentry.metrics().count(
            eventName,
            1.0,
            null,
            SentryMetricsParameters.create(attributes),
        )
    }
}
