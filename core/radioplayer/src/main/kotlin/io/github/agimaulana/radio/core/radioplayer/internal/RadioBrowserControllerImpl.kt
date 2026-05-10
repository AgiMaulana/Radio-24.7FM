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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

internal class RadioBrowserControllerImpl : RadioBrowserController {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val pinnedInvalidations = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    private var browser: MediaBrowser? = null
    private var pinnedSubscriptionJob: Job? = null

    override val pinnedStations: Flow<List<RadioMediaItem>> = flow {
        var lastPinned = emptyList<RadioMediaItem>()
        lastPinned = runCatching { getPinned() }
            .getOrElse { error ->
                if (error is CancellationException) throw error
                Timber.tag(TAG).w(error, "Failed to refresh pinned stations")
                lastPinned
            }
        emit(lastPinned)
        pinnedInvalidations.collect {
            lastPinned = runCatching { getPinned() }
                .getOrElse { error ->
                    if (error is CancellationException) throw error
                    Timber.tag(TAG).w(error, "Failed to refresh pinned stations")
                    lastPinned
                }
            emit(lastPinned)
        }
    }

    val browserListener = object : MediaBrowser.Listener {
        override fun onChildrenChanged(
            browser: MediaBrowser,
            parentId: String,
            itemCount: Int,
            params: MediaLibraryService.LibraryParams?
        ) {
            if (parentId == RadioLibraryContract.PINNED_MEDIA_ID) {
                pinnedInvalidations.tryEmit(Unit)
            }
        }
    }

    fun attach(browser: MediaBrowser) {
        this.browser = browser
        pinnedSubscriptionJob?.cancel()
        pinnedSubscriptionJob = scope.launch {
            withContext(Dispatchers.Main.immediate) {
                browser.subscribe(RadioLibraryContract.PINNED_MEDIA_ID, null).await()
            }
        }
    }

    override suspend fun getPinned(): List<RadioMediaItem> {
        val mediaBrowser = browser ?: return emptyList()
        return withContext(Dispatchers.Main.immediate) {
            val result = mediaBrowser.getChildren(
                RadioLibraryContract.PINNED_MEDIA_ID,
                0,
                Int.MAX_VALUE,
                null
            ).await()
            result.value.orEmpty().map { it.toRadioMediaItem() }
        }
    }

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
        pinnedSubscriptionJob?.cancel()
        browser?.unsubscribe(RadioLibraryContract.PINNED_MEDIA_ID)
        browser?.release()
        browser = null
        scope.cancel()
    }

    private companion object {
        const val TAG = "RadioBrowserController"
    }
}
