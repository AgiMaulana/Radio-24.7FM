package io.github.agimaulana.radio.core.radioplayer

import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RadioMediaItemTest {

    @Test
    fun toMediaItem_attachesPlaybackContextExtras() {
        val radio = RadioMediaItem(
            mediaId = "id1",
            streamUrl = "https://example.com/stream",
            radioMetadata = RadioMediaItem.RadioMetadata(
                stationName = "Station One",
                genre = "Jazz",
                imageUrl = "https://example.com/image.png"
            )
        )

        val mediaItem = radio.toMediaItem(contextType = "SEARCH", contextQuery = "gen", page = 2)
        val extras = mediaItem.mediaMetadata.extras

        assertNotNull("Extras should not be null", extras)
        assertEquals("SEARCH", extras?.getString(PlaybackExtras.KEY_CONTEXT_TYPE))
        assertEquals("gen", extras?.getString(PlaybackExtras.KEY_CONTEXT_QUERY))
        assertEquals(2, extras?.getInt(PlaybackExtras.KEY_CONTEXT_PAGE))
    }

    @Test
    fun toMediaItem_attachesLocationExtras() {
        val radio = RadioMediaItem(
            mediaId = "id1",
            streamUrl = "https://example.com/stream",
            radioMetadata = RadioMediaItem.RadioMetadata(
                stationName = "Station One",
                genre = "Jazz",
                imageUrl = "https://example.com/image.png"
            )
        )

        val mediaItem = radio.toMediaItem(
            contextType = "LOCATION",
            contextLat = -6.2,
            contextLon = 106.8
        )
        val extras = mediaItem.mediaMetadata.extras

        assertNotNull("Extras should not be null", extras)
        assertEquals("LOCATION", extras?.getString(PlaybackExtras.KEY_CONTEXT_TYPE))
        assertEquals(-6.2, extras?.getDouble(PlaybackExtras.KEY_CONTEXT_LAT) ?: 0.0, 0.001)
        assertEquals(106.8, extras?.getDouble(PlaybackExtras.KEY_CONTEXT_LON) ?: 0.0, 0.001)
    }
}
