package io.github.agimaulana.radio.core.radioplayer.internal

import android.os.Bundle
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import io.github.agimaulana.radio.core.radioplayer.PlaybackExtras
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PlaylistPaginatorTest {

    private val player = mockk<Player>(relaxed = true)
    private val catalog = mockk<RadioLibraryCatalog>(relaxed = true)

    @Test
    fun whenPinnedContext_thenDoesNotLoadNextPage() = runTest {
        val pinnedItem = MediaItem.Builder()
            .setMediaId("pinned-1")
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setExtras(Bundle().apply {
                        putString(PlaybackExtras.KEY_CONTEXT_TYPE, PlaybackExtras.TYPE_PINNED)
                    })
                    .build()
            )
            .build()

        every { player.currentMediaItem } returns pinnedItem
        every { player.currentMediaItemIndex } returns 0
        every { player.mediaItemCount } returns 1

        val paginator = PlaylistPaginator(player, catalog, TestScope(testScheduler))

        paginator.reset()
        advanceUntilIdle()

        coVerify(exactly = 0) { catalog.loadChildren(any(), any()) }
    }
}
