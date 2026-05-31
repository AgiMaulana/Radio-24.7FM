package io.github.agimaulana.radio.feature.widget

import android.graphics.Bitmap

data class PinnedTile(
    val mediaId: String,
    val name: String,
    val frequency: String,
    val shortName: String,
    val brandColor: Int,
    val isPlaying: Boolean = false,
    val imageUrl: String? = null,
    val imageBitmap: Bitmap? = null,
)
