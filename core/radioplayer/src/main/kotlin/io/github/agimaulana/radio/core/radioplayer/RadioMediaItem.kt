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

private const val EXTRA_STREAM_URL = "radio_stream_url"

internal fun RadioMediaItem.toMediaItem(
    contextType: String? = null,
    contextQuery: String? = null,
    page: Int? = null
): MediaItem {
    val metadataBuilder = MediaMetadata.Builder()
        .setTitle(radioMetadata.stationName)
        .setSubtitle(radioMetadata.genre)
        .setArtworkUri(radioMetadata.imageUrl.toUri())

    // Attach minimal playback context so the service can restore context after process death
    val extras = android.os.Bundle()
    extras.putString(EXTRA_STREAM_URL, streamUrl)
    contextType?.let { extras.putString("playback_context_type", it) }
    contextQuery?.let { extras.putString("playback_context_query", it) }
    page?.let { extras.putInt("playback_context_page", it) }

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
