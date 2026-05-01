package io.github.agimaulana.radio.core.radioplayer.internal

import android.os.Bundle
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import io.github.agimaulana.radio.core.radioplayer.RadioMediaItem
import io.github.agimaulana.radio.core.radioplayer.RadioMediaItem.RadioMetadata
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
@Suppress("UNCHECKED_CAST")
class PlaybackManagerTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun fetchNextPage_appends_filtered_items_and_increments_maxPageLoaded() = runBlocking {
        val player = mockk<Player>(relaxed = true)

        val fetched = listOf(
            RadioMediaItem("id1", "url1", RadioMetadata("S1", "G", "img")),
            RadioMediaItem("id2", "url2", RadioMetadata("S2", "G", "img"))
        )

        val manager = PlaybackManager(player) { nextPage, query -> fetched }

        val loadedIdsField = PlaybackManager::class.java.getDeclaredField("_loadedIds")
        loadedIdsField.isAccessible = true
        val loadedIds = loadedIdsField.get(manager) as MutableSet<String>
        loadedIds.add("id1")

        manager.fetchNextPage()

        val loadedIdsAfter = loadedIdsField.get(manager) as MutableSet<String>
        assertTrue(loadedIdsAfter.contains("id2"))

        val maxPageField = PlaybackManager::class.java.getDeclaredField("maxPageLoaded")
        maxPageField.isAccessible = true
        val maxPageValue = maxPageField.getInt(manager)
        assertEquals(2, maxPageValue)
    }

    @Test
    fun restoreFromPlayer_sets_playbackContext_and_estimates_maxPageLoaded() {
        val player = mockk<Player>(relaxed = true)

        val extras = Bundle().apply {
            putString("playback_context_type", "SEARCH")
            putString("playback_context_query", "gen")
        }
        val mediaItem = MediaItem.Builder()
            .setMediaId("idX")
            .setMediaMetadata(MediaMetadata.Builder().setExtras(extras).build())
            .build()

        every { player.currentMediaItem } returns mediaItem
        every { player.mediaItemCount } returns 25

        val manager = PlaybackManager(player)
        manager.restoreFromPlayer()

        val maxPageField = PlaybackManager::class.java.getDeclaredField("maxPageLoaded")
        maxPageField.isAccessible = true
        val maxPageValue = maxPageField.getInt(manager)
        assertEquals(3, maxPageValue)

        val minPageField = PlaybackManager::class.java.getDeclaredField("minPageLoaded")
        minPageField.isAccessible = true
        val minPageValue = minPageField.getInt(manager)
        assertEquals(1, minPageValue)

        val playbackContextField = PlaybackManager::class.java.getDeclaredField("_playbackContext")
        playbackContextField.isAccessible = true
        val pc = playbackContextField.get(manager)
        val typeField = pc!!::class.java.getDeclaredField("type")
        typeField.isAccessible = true
        val typeValue = typeField.get(pc).toString()
        val queryField = pc::class.java.getDeclaredField("query")
        queryField.isAccessible = true
        val queryValue = queryField.get(pc) as String?

        assertEquals("SEARCH", typeValue)
        assertEquals("gen", queryValue)
    }

    @Test
    fun fetchPrevPage_prepends_filtered_items_decrements_minPageLoaded_and_seeks() = runBlocking {
        val player = mockk<Player>(relaxed = true)
        every { player.currentMediaItemIndex } returns 0
        every { player.mediaItemCount } returns 10

        val fetched = listOf(
            RadioMediaItem("prev1", "url1", RadioMetadata("P1", "G", "img")),
            RadioMediaItem("prev2", "url2", RadioMetadata("P2", "G", "img"))
        )

        val manager = PlaybackManager(player) { nextPage, query -> fetched }

        val minPageField = PlaybackManager::class.java.getDeclaredField("minPageLoaded")
        minPageField.isAccessible = true
        val maxPageField = PlaybackManager::class.java.getDeclaredField("maxPageLoaded")
        maxPageField.isAccessible = true
        val loadedIdsField = PlaybackManager::class.java.getDeclaredField("_loadedIds")
        loadedIdsField.isAccessible = true

        minPageField.setInt(manager, 3)
        maxPageField.setInt(manager, 5)

        manager.fetchPrevPage()

        val minPageValue = minPageField.getInt(manager)
        assertEquals(2, minPageValue)

        val loadedIds = loadedIdsField.get(manager) as MutableSet<String>
        assertTrue(loadedIds.contains("prev1"))
        assertTrue(loadedIds.contains("prev2"))
    }

    @Test
    fun fetchPrevPage_skips_when_minPageLoaded_is_1() = runBlocking {
        val player = mockk<Player>(relaxed = true)

        val manager = PlaybackManager(player) { _, _ -> emptyList() }

        val minPageField = PlaybackManager::class.java.getDeclaredField("minPageLoaded")
        minPageField.isAccessible = true
        minPageField.setInt(manager, 1)

        manager.fetchPrevPage()

        val minPageValue = minPageField.getInt(manager)
        assertEquals(1, minPageValue)
    }
}