package io.github.agimaulana.radio.core.radioplayer

import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata

data class RadioMediaItem(
    val mediaId: String,
    val streamUrl: String,
    val radioMetadata: RadioMetadata,
) {

    data class RadioMetadata(
        val stationName: String,
        val genre: String,
        val imageUrl: String,
    )
}

object PlaybackExtras {
    const val KEY_STREAM_URL = "radio_stream_url"
    const val KEY_CONTEXT_TYPE = "playback_context_type"
    const val KEY_CONTEXT_QUERY = "playback_context_query"
    const val KEY_CONTEXT_PAGE = "playback_context_page"
    const val KEY_CONTEXT_LAT = "playback_context_lat"
    const val KEY_CONTEXT_LON = "playback_context_lon"

    const val TYPE_PINNED = "PINNED"
    const val TYPE_SEARCH = "SEARCH"
    const val TYPE_ALL = "ALL"
    const val TYPE_LOCATION = "LOCATION"
}

private const val EXTRA_STREAM_URL = PlaybackExtras.KEY_STREAM_URL

internal fun RadioMediaItem.toMediaItem(
    contextType: String? = null,
    contextQuery: String? = null,
    page: Int? = null,
    contextLat: Double? = null,
    contextLon: Double? = null,
): MediaItem {
    val metadataBuilder = MediaMetadata.Builder()
        .setTitle(radioMetadata.stationName)
        .setSubtitle(radioMetadata.genre)
        .setArtworkUri(radioMetadata.imageUrl.toUri())

    val extras = android.os.Bundle()
    extras.putString(EXTRA_STREAM_URL, streamUrl)
    contextType?.let { extras.putString(PlaybackExtras.KEY_CONTEXT_TYPE, it) }
    contextQuery?.let { extras.putString(PlaybackExtras.KEY_CONTEXT_QUERY, it) }
    page?.let { extras.putInt(PlaybackExtras.KEY_CONTEXT_PAGE, it) }
    contextLat?.let { extras.putDouble(PlaybackExtras.KEY_CONTEXT_LAT, it) }
    contextLon?.let { extras.putDouble(PlaybackExtras.KEY_CONTEXT_LON, it) }

    if (!extras.isEmpty) {
        metadataBuilder.setExtras(extras)
    }

    return MediaItem.Builder()
        .setMediaId(mediaId)
        .setUri(streamUrl.takeIf { it.isNotBlank() }?.toUri())
        .setMediaMetadata(metadataBuilder.build())
        .build()
}

fun MediaItem.toRadioMediaItem() = RadioMediaItem(
    mediaId = mediaId,
    streamUrl = localConfiguration?.uri?.toString().orEmpty().ifBlank {
        mediaMetadata.extras?.getString(EXTRA_STREAM_URL).orEmpty()
    },
    radioMetadata = RadioMediaItem.RadioMetadata(
        stationName = mediaMetadata.title?.toString().orEmpty(),
        genre = mediaMetadata.subtitle?.toString().orEmpty(),
        imageUrl = mediaMetadata.artworkUri?.toString().orEmpty()
    )
)
