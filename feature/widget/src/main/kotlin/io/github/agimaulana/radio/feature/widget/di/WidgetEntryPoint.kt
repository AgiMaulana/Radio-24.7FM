package io.github.agimaulana.radio.feature.widget.di

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.agimaulana.radio.core.radioplayer.RadioPlayerControllerFactory
import io.github.agimaulana.radio.domain.api.usecase.GetPinnedStationsUseCase
import io.github.agimaulana.radio.domain.api.usecase.GetRadioStationUseCase

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun getPinnedStationsUseCase(): GetPinnedStationsUseCase
    fun getRadioStationUseCase(): GetRadioStationUseCase
    fun radioPlayerControllerFactory(): RadioPlayerControllerFactory
}
