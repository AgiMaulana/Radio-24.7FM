package io.github.agimaulana.radio.feature.stationlist

import io.github.agimaulana.radio.core.tracker.EventTracker
import javax.inject.Inject

class StationListTracker @Inject constructor(
    private val eventTracker: EventTracker,
) {
    fun trackScreenViewed() {
        eventTracker.track("station_list_screen_viewed")
    }

    fun trackLoadMore(page: Int) {
        eventTracker.track(
            eventName = "station_list_load_more_requested",
            properties = mapOf("page" to page.toString()),
        )
    }

    fun trackSearchSubmitted(query: String) {
        eventTracker.track(
            eventName = "station_list_search_submitted",
            properties = mapOf("query" to query),
        )
    }

    fun trackStationSelected(
        stationId: String,
        stationName: String,
    ) {
        eventTracker.track(
            eventName = "station_selected",
            properties = mapOf(
                "station_id" to stationId,
                "station_name" to stationName,
            ),
        )
    }

    fun trackPlaybackPaused(stationId: String?) {
        eventTracker.track(
            eventName = "station_playback_paused",
            properties = stationId.toProperties(),
        )
    }

    fun trackPlaybackResumed(stationId: String?) {
        eventTracker.track(
            eventName = "station_playback_resumed",
            properties = stationId.toProperties(),
        )
    }

    fun trackPlaybackStopped(stationId: String?) {
        eventTracker.track(
            eventName = "station_playback_stopped",
            properties = stationId.toProperties(),
        )
    }

    fun trackPlayerExpanded(
        source: String,
        stationId: String?,
        stationName: String?,
        isPlaying: Boolean,
    ) {
        eventTracker.track(
            eventName = "station_player_expanded",
            properties = buildMap {
                put("source", source)
                put("is_playing", isPlaying.toString())
                stationId?.takeIf { it.isNotBlank() }?.let { put("station_id", it) }
                stationName?.takeIf { it.isNotBlank() }?.let { put("station_name", it) }
            },
        )
    }

    fun trackPlayerCollapsed(
        source: String,
        stationId: String?,
        stationName: String?,
        isPlaying: Boolean,
    ) {
        eventTracker.track(
            eventName = "station_player_collapsed",
            properties = buildMap {
                put("source", source)
                put("is_playing", isPlaying.toString())
                stationId?.takeIf { it.isNotBlank() }?.let { put("station_id", it) }
                stationName?.takeIf { it.isNotBlank() }?.let { put("station_name", it) }
            },
        )
    }

    private fun String?.toProperties(): Map<String, String> {
        return if (isNullOrBlank()) emptyMap() else mapOf("station_id" to this)
    }
}
