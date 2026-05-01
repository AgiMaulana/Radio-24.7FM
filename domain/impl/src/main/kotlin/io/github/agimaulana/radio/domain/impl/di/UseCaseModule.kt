package io.github.agimaulana.radio.domain.impl.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import io.github.agimaulana.radio.domain.api.usecase.GetPinnedStationsUseCase
import io.github.agimaulana.radio.domain.api.usecase.GetRadioStationUseCase
import io.github.agimaulana.radio.domain.api.usecase.GetRadioStationsUseCase
import io.github.agimaulana.radio.domain.api.usecase.PinStationUseCase
import io.github.agimaulana.radio.domain.api.usecase.UnpinStationUseCase
import io.github.agimaulana.radio.domain.impl.GetPinnedStationsUseCaseImpl
import io.github.agimaulana.radio.domain.impl.GetRadioStationUseCaseImpl
import io.github.agimaulana.radio.domain.impl.GetRadioStationsUseCaseImpl
import io.github.agimaulana.radio.domain.impl.PinStationUseCaseImpl
import io.github.agimaulana.radio.domain.impl.UnpinStationUseCaseImpl

@Module
@InstallIn(ViewModelComponent::class)
interface UseCaseModule {

    @Binds
    fun bindGetRadioStationUseCase(impl: GetRadioStationUseCaseImpl): GetRadioStationUseCase

    @Binds
    fun bindPinStationUseCase(impl: PinStationUseCaseImpl): PinStationUseCase

    @Binds
    fun bindUnpinStationUseCase(impl: UnpinStationUseCaseImpl): UnpinStationUseCase

    @Binds
    fun bindGetPinnedStationsUseCase(impl: GetPinnedStationsUseCaseImpl): GetPinnedStationsUseCase
}
