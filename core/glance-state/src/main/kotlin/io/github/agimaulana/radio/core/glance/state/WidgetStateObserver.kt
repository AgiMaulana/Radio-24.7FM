package io.github.agimaulana.radio.core.glance.state

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.GlanceId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import io.github.agimaulana.radio.core.glance.state.ApplicationCoroutineScope

/**
 * Observes flows and triggers widget updates.
 * The observer runs on the provided application-level [CoroutineScope].
 */
@Singleton
public class WidgetStateObserver @Inject constructor(
    private val context: Context,
    @ApplicationCoroutineScope private val appScope: CoroutineScope
) {

    public fun <T> observeAndUpdate(
        widgetClass: Class<out GlanceAppWidget>,
        stateFlow: Flow<T>,
        debounceMillis: Long = 0L,
        onUpdate: suspend (Context, GlanceId, T) -> Unit
    ): Job {
        val job = SupervisorJob()
        val scope = CoroutineScope(appScope.coroutineContext + job)

        scope.launch {
            val flow = if (debounceMillis > 0) stateFlow.debounce(debounceMillis) else stateFlow
            flow.collect { latest ->
                updateAllWidgets(context, widgetClass, latest, onUpdate)
            }
        }

        return job
    }

    public fun stopObserving(job: Job) {
        job.cancel()
    }
}
