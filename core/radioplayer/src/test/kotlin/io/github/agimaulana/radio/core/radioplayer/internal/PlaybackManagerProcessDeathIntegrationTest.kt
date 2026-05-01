package io.github.agimaulana.radio.core.radioplayer.internal

import android.os.Bundle
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.exoplayer.ExoPlayer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class PlaybackManagerProcessDeathIntegrationTest {

    private lateinit var player: ExoPlayer
    private lateinit var playbackManager: PlaybackManager

    @Before
    fun setup() {
        val context = RuntimeEnvironment.application
        player = ExoPlayer.Builder(context).build()
        playbackManager = PlaybackManager(player) { _, _ -> emptyList() }
    }

    @Test
    fun restoreFromPlayer_restoresSearchContext() {
        player.addMediaItem(createMediaItem("id1", "SEARCH", "rock"))
        player.addMediaItem(createMediaItem("id2", "SEARCH", "rock"))
        player.addMediaItem(createMediaItem("id3", "SEARCH", "rock"))
        player.seekTo(1, 0L)
        player.prepare()

        playbackManager.restoreFromPlayer()

        assertEquals(PlaybackManager.PlaybackContext.Type.SEARCH, playbackManager.playbackContext.type)
        assertEquals("rock", playbackManager.playbackContext.query)
        assertEquals(setOf("id1", "id2", "id3"), playbackManager.loadedIds)
    }

    @Test
    fun restoreFromPlayer_restoresDefaultContext() {
        player.addMediaItem(createMediaItem("id1", "DEFAULT", null))
        player.addMediaItem(createMediaItem("id2", "DEFAULT", null))
        player.prepare()

        playbackManager.restoreFromPlayer()

        assertEquals(PlaybackManager.PlaybackContext.Type.DEFAULT, playbackManager.playbackContext.type)
        assertEquals(null, playbackManager.playbackContext.query)
        assertEquals(setOf("id1", "id2"), playbackManager.loadedIds)
    }

    @Test
    fun restoreFromPlayer_restoresPinnedContext() {
        player.addMediaItem(createMediaItem("pin1", "PINNED", null))
        player.addMediaItem(createMediaItem("pin2", "PINNED", null))
        player.prepare()

        playbackManager.restoreFromPlayer()

        assertEquals(PlaybackManager.PlaybackContext.Type.PINNED, playbackManager.playbackContext.type)
        assertEquals(null, playbackManager.playbackContext.query)
    }

    @Test
    fun restoreFromPlayer_EstimatesNextPage() {
        for (i in 1..25) {
            player.addMediaItem(createMediaItem("id$i", "DEFAULT", null))
        }
        player.prepare()

        playbackManager.restoreFromPlayer()

        assertTrue(playbackManager.maxPageLoaded >= 3)
    }

    @Test
    fun restoreFromPlayer_emptyPlayer_doesNotThrow() {
        player.prepare()

        playbackManager.restoreFromPlayer()

        assertEquals(PlaybackManager.PlaybackContext.Type.DEFAULT, playbackManager.playbackContext.type)
        assertTrue(playbackManager.loadedIds.isEmpty())
    }

    private fun createMediaItem(id: String, contextType: String, query: String?): MediaItem {
        val extras = Bundle()
        extras.putString("playback_context_type", contextType)
        if (query != null) {
            extras.putString("playback_context_query", query)
        }
        val metadata = MediaMetadata.Builder()
            .setExtras(extras)
            .build()
        return MediaItem.Builder()
            .setMediaId(id)
            .setMediaMetadata(metadata)
            .setUri("https://example.com/stream")
            .build()
    }
}