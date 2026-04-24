package io.github.agimaulana.radio.domain.impl.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import io.github.agimaulana.radio.domain.api.usecase.GetRadioStationUseCase
import io.github.agimaulana.radio.domain.api.usecase.GetRadioStationsUseCase
import io.github.agimaulana.radio.domain.impl.GetRadioStationUseCaseImpl
import io.github.agimaulana.radio.domain.impl.GetRadioStationsUseCaseImpl

@Module
@InstallIn(ViewModelComponent::class)
interface UseCaseModule {

    @Binds
    fun bindGetRadioStationsUseCase(impl: GetRadioStationsUseCaseImpl): GetRadioStationsUseCase

    @Binds
    fun bindGetRadioStationUseCase(impl: GetRadioStationUseCaseImpl): GetRadioStationUseCase
}
