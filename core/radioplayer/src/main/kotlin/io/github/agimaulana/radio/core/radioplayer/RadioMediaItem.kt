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

internal fun RadioMediaItem.toMediaItem(): MediaItem {
    return MediaItem.Builder()
        .setMediaId(mediaId)
        .setUri(streamUrl)
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(radioMetadata.stationName)
                .setSubtitle(radioMetadata.genre)
                .setArtworkUri(radioMetadata.imageUrl.toUri())
                .build()
        )
        .build()
}
