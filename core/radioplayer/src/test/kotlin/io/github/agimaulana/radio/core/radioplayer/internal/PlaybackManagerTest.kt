package io.github.agimaulana.radio.core.radioplayer.internal

import android.os.Bundle
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import io.github.agimaulana.radio.core.radioplayer.RadioMediaItem
import io.github.agimaulana.radio.core.radioplayer.RadioMediaItem.RadioMetadata
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.Assert.*
import org.junit.Before
import org.junit.After
import org.junit.Test
import org.robolectric.RobolectricTestRunner
import org.junit.runner.RunWith
import io.mockk.every
import io.mockk.mockk

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
    fun fetchNextPage_appends_filtered_items_and_increments_nextPage() = runBlocking {
        val player = mockk<Player>(relaxed = true)

        val fetched = listOf(
            RadioMediaItem("id1", "url1", RadioMetadata("S1", "G", "img")),
            RadioMediaItem("id2", "url2", RadioMetadata("S2", "G", "img"))
        )

        val manager = PlaybackManager(player) { nextPage, query -> fetched }

        // simulate that id1 was already loaded to test dedupe
        val loadedIdsField = PlaybackManager::class.java.getDeclaredField("_loadedIds")
        loadedIdsField.isAccessible = true
        val loadedIds = loadedIdsField.get(manager) as MutableSet<String>
        loadedIds.add("id1")

        // call fetchNextPage
        manager.fetchNextPage()

        // verify that loadedIds contains only id2 as newly added
        val loadedIdsAfter = loadedIdsField.get(manager) as MutableSet<String>
        assertTrue(loadedIdsAfter.contains("id2"))

        // nextPage should be incremented to 2
        val nextPageField = PlaybackManager::class.java.getDeclaredField("nextPage")
        nextPageField.isAccessible = true
        val nextPageValue = nextPageField.getInt(manager)
        assertEquals(2, nextPageValue)
    }

    @Test
    fun restoreFromPlayer_sets_playbackContext_and_estimates_nextPage() {
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

        // nextPage should be (25 / PAGE_SIZE) + 1 => (25/10)+1 = 3
        val nextPageField = PlaybackManager::class.java.getDeclaredField("nextPage")
        nextPageField.isAccessible = true
        val nextPageValue = nextPageField.getInt(manager)
        assertEquals(3, nextPageValue)

        // playbackContext should be SEARCH with query "gen"
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
}
