package io.github.agimaulana.radio.core.radioplayer.internal

import androidx.media3.session.MediaController
import io.github.agimaulana.radio.core.radioplayer.RadioMediaItem
import io.github.agimaulana.radio.core.radioplayer.RadioMediaItem.RadioMetadata
import io.github.agimaulana.radio.core.radioplayer.RadioPlayerController
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verifyOrder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RadioPlayerControllerImplTest {

    private val mediaController = mockk<MediaController>(relaxed = true)

    @Test
    fun startPlayback_setsPlaylist_and_startsPlayer() {
        val controller = RadioPlayerControllerImpl(mediaController)
        val items = listOf(
            RadioMediaItem("id-1", "https://example.com/1", RadioMetadata("One", "News", "https://example.com/1.png")),
            RadioMediaItem("id-2", "https://example.com/2", RadioMetadata("Two", "Talk", "https://example.com/2.png"))
        )

        controller.startPlayback(
            items = items,
            startIndex = 1,
            context = RadioPlayerController.PlaybackContext(
                RadioPlayerController.PlaybackContext.Type.SEARCH,
                query = "news"
            )
        )

        verifyOrder {
            mediaController.setMediaItems(any(), 1, 0L)
            mediaController.prepare()
            mediaController.play()
        }
    }

    @Test
    fun startPlayback_withLocationContext_attachesLocationExtras() {
        val controller = RadioPlayerControllerImpl(mediaController)
        val items = listOf(
            RadioMediaItem("id-1", "https://example.com/1", RadioMetadata("One", "News", "https://example.com/1.png"))
        )

        controller.startPlayback(
            items = items,
            startIndex = 0,
            context = RadioPlayerController.PlaybackContext(
                type = RadioPlayerController.PlaybackContext.Type.LOCATION,
                location = RadioPlayerController.PlaybackContext.Location(-6.2, 106.8)
            )
        )

        val slot = slot<List<androidx.media3.common.MediaItem>>()
        verifyOrder {
            mediaController.setMediaItems(capture(slot), 0, 0L)
            mediaController.prepare()
            mediaController.play()
        }

        val mediaItems = slot.captured
        assertEquals(1, mediaItems.size)
        val extras = mediaItems[0].mediaMetadata.extras
        assertNotNull(extras)
        assertEquals("LOCATION", extras?.getString("playback_context_type"))
        assertEquals(-6.2, extras?.getDouble("playback_context_lat") ?: 0.0, 0.001)
        assertEquals(106.8, extras?.getDouble("playback_context_lon") ?: 0.0, 0.001)
    }
}
