package io.github.agimaulana.radio.core.radioplayer

import android.content.ComponentName
import android.content.Context
import androidx.media3.session.MediaBrowser
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.agimaulana.radio.core.radioplayer.internal.RadioBrowserControllerImpl
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class RadioBrowserFactory @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    suspend fun get(): RadioBrowserController = suspendCancellableCoroutine { cont ->
        val controller = RadioBrowserControllerImpl()
        val sessionToken = SessionToken(context, ComponentName(context, RadioService::class.java))
        val browserFuture = MediaBrowser.Builder(context, sessionToken)
            .setListener(controller.browserListener)
            .buildAsync()

        cont.invokeOnCancellation {
            browserFuture.cancel(true)
        }

        browserFuture.addListener({
            runCatching {
                browserFuture.get()
            }.onSuccess { browser ->
                controller.attach(browser)
                if (cont.isActive) {
                    cont.resume(controller)
                }
            }.onFailure { error ->
                if (cont.isActive) {
                    cont.resumeWithException(error)
                }
            }
        }, MoreExecutors.directExecutor())
    }
}
