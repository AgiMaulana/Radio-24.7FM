package io.github.agimaulana.radio.core.glance.state

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.GlanceId
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.ConcurrentHashMap

/**
 * Central manager for widget state observation. Keeps a map of active observer Jobs keyed by widget class.
 *
 * Behaviour:
 * - observe(...) will return an existing Job if already observing for the widgetClass.
 * - otherwise it will obtain the WidgetStateObserver via Hilt EntryPoint and start observing;
 *   the returned Job is stored in the internal map.
 * - on each emission, the provided onUpdate is invoked for all GlanceIds. After invoking the updater,
 *   the manager checks whether there are any active widget instances; if none remain, the Job is cancelled
 *   and removed from the map (auto-stop).
 */
public object WidgetStateManager {
    private val jobs = ConcurrentHashMap<Class<out GlanceAppWidget>, Job>()

    @JvmStatic
    public fun <T> observe(
        context: Context,
        widgetClass: Class<out GlanceAppWidget>,
        stateFlow: Flow<T>,
        debounceMillis: Long = 0L,
        onUpdate: suspend (Context, GlanceId, T) -> Unit,
    ): Job {
        // If already observing and job is active, return it.
        val existing = jobs[widgetClass]
        if (existing != null && existing.isActive) return existing

        val job = observeWidgetState(
            context = context,
            widgetClass = widgetClass,
            stateFlow = stateFlow,
            debounceMillis = debounceMillis,
            onUpdate = onUpdate,
        )

        jobs[widgetClass] = job
        return job
    }

    @JvmStatic
    public fun stopObserving(widgetClass: Class<out GlanceAppWidget>) {
        val job = jobs.remove(widgetClass)
        job?.cancel()
    }

    @JvmStatic
    public fun stopAll() {
        val entries = jobs.keys.toList()
        for (k in entries) stopObserving(k)
    }
}
