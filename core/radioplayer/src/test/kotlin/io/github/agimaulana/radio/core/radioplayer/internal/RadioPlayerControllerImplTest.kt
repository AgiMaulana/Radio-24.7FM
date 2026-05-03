package io.github.agimaulana.radio.core.radioplayer.internal

import androidx.media3.session.MediaController
import io.github.agimaulana.radio.core.radioplayer.RadioMediaItem
import io.github.agimaulana.radio.core.radioplayer.RadioMediaItem.RadioMetadata
import io.github.agimaulana.radio.core.radioplayer.RadioPlayerController
import io.mockk.mockk
import io.mockk.verifyOrder
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
}
