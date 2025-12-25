package io.github.agimaulana.radio.core.radioplayer

import android.content.ComponentName
import android.content.Context
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.agimaulana.radio.core.radioplayer.internal.RadioPlayerControllerImpl
import javax.inject.Inject

class RadioPlayerControllerFactory @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    fun getAsync(block: (RadioPlayerController) -> Unit) {
        val sessionToken = SessionToken(context, ComponentName(context, RadioService::class.java))
        val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()

        controllerFuture.addListener({
            val controller = RadioPlayerControllerImpl(controllerFuture.get())
            block(controller)
        }, MoreExecutors.directExecutor())
    }
}