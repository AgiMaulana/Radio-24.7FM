package io.github.agimaulana.radio.infrastructure.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import io.github.agimaulana.radio.domain.api.repository.RadioStationRepository
import io.github.agimaulana.radio.domain.api.repository.SampleUserRepository
import io.github.agimaulana.radio.infrastructure.repository.RadioStationRepositoryImpl
import io.github.agimaulana.radio.infrastructure.repository.SampleUserRepositoryImpl

@Module
@InstallIn(ViewModelComponent::class)
interface RepositoryModule {

    @Binds
    fun bind(impl: SampleUserRepositoryImpl): SampleUserRepository

    @Binds
    fun bindRadioStationRepository(impl: RadioStationRepositoryImpl): RadioStationRepository
}
