package io.github.agimaulana.radio.core.radioplayer

import android.content.ComponentName
import android.content.Context
import androidx.media3.session.MediaBrowser
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.agimaulana.radio.core.radioplayer.internal.RadioBrowserControllerImpl
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class RadioBrowserFactory @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    suspend fun get(): RadioBrowserController = suspendCoroutine { cont ->
        val controller = RadioBrowserControllerImpl()
        val sessionToken = SessionToken(context, ComponentName(context, RadioService::class.java))
        val browserFuture = MediaBrowser.Builder(context, sessionToken)
            .setListener(controller.browserListener)
            .buildAsync()

        browserFuture.addListener({
            controller.attach(browserFuture.get())
            cont.resume(controller)
        }, MoreExecutors.directExecutor())
    }
}
