package io.github.agimaulana.radio.core.glance.viewmodel.hilt

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
public interface GlanceViewModelFactoryEntryPoint {
    public fun getViewModelFactory(): GlanceAppViewModelFactory
}
