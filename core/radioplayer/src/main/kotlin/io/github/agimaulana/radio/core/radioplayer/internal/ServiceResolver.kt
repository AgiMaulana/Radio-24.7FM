package io.github.agimaulana.radio.core.radioplayer.internal

import io.github.agimaulana.radio.core.radioplayer.RadioMediaItem

/**
 * Small resolver to avoid importing domain types into the core module.
 * Other modules can register a lambda that fetches domain stations and maps to RadioMediaItem.
 */
object ServiceResolver {
    @Volatile
    private var resolver: (suspend (page: Int, query: String?) -> List<RadioMediaItem>)? = null

    @Volatile
    private var playbackStartCallback: (suspend (items: List<RadioMediaItem>, startIndex: Int, contextType: String?, contextQuery: String?) -> Unit)? = null

    fun registerGetRadioStationsResolver(
        r: suspend (page: Int, query: String?) -> List<RadioMediaItem>
    ) {
        resolver = r
    }

    fun clearGetRadioStationsResolver() {
        resolver = null
    }

    fun resolveGetRadioStationsResolver():
        (suspend (page: Int, query: String?) -> List<RadioMediaItem>)? = resolver

    fun registerPlaybackStartCallback(
        callback: suspend (items: List<RadioMediaItem>, startIndex: Int, contextType: String?, contextQuery: String?) -> Unit
    ) {
        playbackStartCallback = callback
    }

    fun resolvePlaybackStartCallback():
        (suspend (items: List<RadioMediaItem>, startIndex: Int, contextType: String?, contextQuery: String?) -> Unit)? = playbackStartCallback
}
