package io.github.agimaulana.radio.core.radioplayer.internal

import android.os.Bundle
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaBrowser
import androidx.media3.session.MediaLibraryService
import io.github.agimaulana.radio.core.radioplayer.RadioBrowserController
import io.github.agimaulana.radio.core.radioplayer.RadioLibraryContract
import io.github.agimaulana.radio.core.radioplayer.RadioMediaItem
import io.github.agimaulana.radio.core.radioplayer.toRadioMediaItem
import io.github.agimaulana.radio.domain.api.entity.GeoLatLong
import io.github.agimaulana.radio.domain.api.entity.RadioStation
import io.github.agimaulana.radio.domain.api.usecase.GetPinnedStationsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class RadioBrowserControllerImpl(
    private val pinnedStationLimit: Int,
    private val getPinnedStationsUseCase: GetPinnedStationsUseCase,
) : RadioBrowserController {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _pinnedStations = MutableStateFlow<List<RadioMediaItem>>(emptyList())
    private var browser: MediaBrowser? = null
    private var pinnedCollectionJob: Job? = null

    override val pinnedStations: Flow<List<RadioMediaItem>> =
        _pinnedStations.asStateFlow()

    internal val browserListener = object : MediaBrowser.Listener {
        override fun onChildrenChanged(
            browser: MediaBrowser,
            parentId: String,
            itemCount: Int,
            params: MediaLibraryService.LibraryParams?
        ) {
        }
    }

    fun attach(browser: MediaBrowser) {
        this.browser = browser
        scope.launch {
            withContext(Dispatchers.Main.immediate) {
                browser.subscribe(RadioLibraryContract.PINNED_MEDIA_ID, null).await()
            }
        }
        observePinnedStations()
    }

    private fun observePinnedStations() {
        pinnedCollectionJob?.cancel()
        pinnedCollectionJob = scope.launch {
            getPinnedStationsUseCase.execute().collect { stations ->
                _pinnedStations.value = stations.map { it.toRadioMediaItem() }
            }
        }
    }

    private fun RadioStation.toRadioMediaItem(): RadioMediaItem {
        return RadioMediaItem(
            mediaId = stationUuid,
            streamUrl = resolvedUrl.ifEmpty { url },
            radioMetadata = RadioMediaItem.RadioMetadata(
                stationName = name,
                genre = tags.firstOrNull().orEmpty(),
                imageUrl = imageUrl
            )
        )
    }

    override suspend fun getPinned(): List<RadioMediaItem> = _pinnedStations.value

    override suspend fun getStation(mediaId: String): RadioMediaItem? {
        val mediaBrowser = browser ?: return null
        return withContext(Dispatchers.Main.immediate) {
            val result = mediaBrowser.getItem(mediaId).await()
            result.value?.toRadioMediaItem()
        }
    }

    @OptIn(UnstableApi::class)
    override suspend fun getStations(
        page: Int,
        pageSize: Int,
        searchName: String?,
        location: GeoLatLong?,
    ): List<RadioMediaItem> {
        val mediaBrowser = browser ?: return emptyList()
        val extras = Bundle().apply {
            searchName?.takeIf { it.isNotBlank() }?.let {
                putString(RadioLibraryContract.EXTRA_SEARCH, it)
            }
            location?.let {
                putDouble(RadioLibraryContract.EXTRA_LOCATION_LAT, it.latitude)
                putDouble(RadioLibraryContract.EXTRA_LOCATION_LON, it.longitude)
            }
        }
        val params = if (extras.isEmpty) {
            null
        } else {
            MediaLibraryService.LibraryParams.Builder()
                .setExtras(extras)
                .build()
        }

        return withContext(Dispatchers.Main.immediate) {
            val result = mediaBrowser.getChildren(
                RadioLibraryContract.STATIONS_MEDIA_ID,
                page,
                pageSize,
                params
            ).await()
            result.value.orEmpty().map { it.toRadioMediaItem() }
        }
    }

    override fun release() {
        pinnedCollectionJob?.cancel()
        browser?.unsubscribe(RadioLibraryContract.PINNED_MEDIA_ID)
        browser?.release()
        browser = null
        scope.cancel()
    }

    private companion object {
        const val TAG = "RadioBrowserController"
    }
}
