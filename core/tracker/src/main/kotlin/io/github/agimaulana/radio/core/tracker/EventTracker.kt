package io.github.agimaulana.radio.core.tracker

interface EventTracker {
    fun track(
        eventName: String,
        properties: Map<String, String> = emptyMap(),
    )
}
