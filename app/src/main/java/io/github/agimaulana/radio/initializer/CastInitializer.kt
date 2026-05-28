package io.github.agimaulana.radio.initializer

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.cast.Cast
import androidx.media3.common.util.UnstableApi
import androidx.startup.Initializer

class CastInitializer : Initializer<Unit> {

    @OptIn(UnstableApi::class)
    override fun create(context: Context) {
        Cast.getSingletonInstance(context).initialize()
    }

    override fun dependencies(): List<Class<out Initializer<*>?>?> = emptyList()
}
