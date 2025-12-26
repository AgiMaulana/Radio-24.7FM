package io.github.agimaulana.radio.core.radioplayer.extension

import io.github.agimaulana.radio.core.radioplayer.RadioMediaItem
import io.github.agimaulana.radio.core.radioplayer.RadioPlayerController

fun RadioPlayerController.playImmediately(radioMediaItem: RadioMediaItem) {
    setMediaItem(radioMediaItem)
    prepare()
    play()
}
