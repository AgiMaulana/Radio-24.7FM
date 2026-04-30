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
        assertEquals("SEARCH", extras?.getString("playback_context_type"))
        assertEquals("gen", extras?.getString("playback_context_query"))
        assertEquals(2, extras?.getInt("playback_context_page"))
    }
}
