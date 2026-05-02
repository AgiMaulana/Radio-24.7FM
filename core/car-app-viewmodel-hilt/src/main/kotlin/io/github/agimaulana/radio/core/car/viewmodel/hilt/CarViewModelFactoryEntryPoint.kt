package io.github.agimaulana.radio.core.car.viewmodel.hilt

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
public interface CarViewModelFactoryEntryPoint {
    // Hilt will automatically implement this and provide the factory
    public fun getViewModelFactory(): CarAppViewModelFactory
}
