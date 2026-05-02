package io.github.agimaulana.radio.core.car.state

import androidx.car.app.Screen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.properties.ReadOnlyProperty

/**
 * Collects a [StateFlow] tied to the Screen's lifecycle.
 * Automatically triggers [Screen.invalidate] on emission.
 */
public fun <T> Screen.collectAsStateWithLifecycle(
    stateFlow: StateFlow<T>,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED
): ReadOnlyProperty<Any?, T> {
    
    // 1. Launch the collection job to trigger UI refreshes
    lifecycleScope.launch {
        lifecycle.repeatOnLifecycle(minActiveState) {
            stateFlow.collect {
                invalidate()
            }
        }
    }

    // 2. Return a delegate that always reads the latest value directly from the StateFlow
    return ReadOnlyProperty { _, _ -> stateFlow.value }
}

/**
 * Collects a standard [Flow] tied to the Screen's lifecycle.
 * Requires an initial value since standard Flows don't hold state.
 */
public fun <T> Screen.collectAsStateWithLifecycle(
    flow: Flow<T>,
    initialValue: T,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED
): ReadOnlyProperty<Any?, T> {
    
    // We must hold the state locally for standard Flows
    var currentState = initialValue

    lifecycleScope.launch {
        lifecycle.repeatOnLifecycle(minActiveState) {
            flow.collect {
                currentState = it
                invalidate()
            }
        }
    }

    return ReadOnlyProperty { _, _ -> currentState }
}