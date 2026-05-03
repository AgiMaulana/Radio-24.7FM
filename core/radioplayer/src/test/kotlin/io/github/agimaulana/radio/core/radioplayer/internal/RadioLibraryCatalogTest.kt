package io.github.agimaulana.radio.core.radioplayer.internal

import io.github.agimaulana.radio.domain.api.entity.RadioStation
import io.github.agimaulana.radio.domain.api.usecase.GetRadioStationsUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RadioLibraryCatalogTest {

    private val getRadioStationsUseCase = mockk<GetRadioStationsUseCase>()

    @Test
    fun rootItem_returnsBrowsableRoot() {
        val catalog = RadioLibraryCatalog(getRadioStationsUseCase)

        val root = catalog.rootItem()

        assertEquals(RadioLibraryCatalog.ROOT_MEDIA_ID, root.mediaId)
        assertTrue(root.mediaMetadata.isBrowsable == true)
        assertEquals("Radio 24.7FM", root.mediaMetadata.title?.toString())
    }

    @Test
    fun loadChildren_maps_stations_to_media_items() = runTest {
        coEvery {
            getRadioStationsUseCase.execute(page = 1, searchName = null, location = null)
        } returns listOf(
            RadioStation(
                stationUuid = "station-1",
                name = "Station One",
                tags = listOf("News"),
                imageUrl = "https://example.com/image.png",
                url = "https://example.com/stream",
                resolvedUrl = "https://example.com/resolved"
            )
        )

        val catalog = RadioLibraryCatalog(getRadioStationsUseCase)

        val children = catalog.loadChildren()

        assertEquals(1, children.size)
        val item = children.single()
        assertEquals("station-1", item.mediaId)
        assertEquals("https://example.com/resolved", item.localConfiguration?.uri.toString())
        assertEquals("Station One", item.mediaMetadata.title?.toString())
        assertEquals("News", item.mediaMetadata.subtitle?.toString())
        assertTrue(item.mediaMetadata.isPlayable == true)
    }
}
