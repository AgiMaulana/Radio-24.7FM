package io.github.agimaulana.radio.core.glance.state

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow

/**
 * Convenience helper that obtains the WidgetStateObserver via Hilt EntryPoint
 * and starts observing the provided Flow, returning the Job so callers can keep
 * a reference to cancel if needed.
 */
public fun <T> observeWidgetState(
    context: Context,
    widgetClass: Class<out GlanceAppWidget>,
    stateFlow: Flow<T>,
    debounceMillis: Long = 0L,
    onUpdate: suspend (Context, GlanceId, T) -> Unit,
): Job {
    val entryPoint = EntryPointAccessors.fromApplication(context, WidgetStateObserverEntryPoint::class.java)
    val observer = entryPoint.widgetStateObserver()
    return observer.observeAndUpdate(
        widgetClass = widgetClass,
        stateFlow = stateFlow,
        debounceMillis = debounceMillis,
        onUpdate = onUpdate,
    )
}
