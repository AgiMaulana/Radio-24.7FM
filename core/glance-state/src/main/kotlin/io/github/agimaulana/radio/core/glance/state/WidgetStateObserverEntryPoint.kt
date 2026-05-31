package io.github.agimaulana.radio.core.glance.state

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Hilt EntryPoint to obtain the WidgetStateObserver from non-Hilt locations (eg. Glance)
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
public interface WidgetStateObserverEntryPoint {
    public fun widgetStateObserver(): WidgetStateObserver
}
