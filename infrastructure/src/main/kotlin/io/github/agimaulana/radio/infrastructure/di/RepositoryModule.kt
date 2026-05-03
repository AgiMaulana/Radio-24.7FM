package io.github.agimaulana.radio.infrastructure.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.agimaulana.radio.domain.api.repository.CatalogStateRepository
import io.github.agimaulana.radio.domain.api.repository.PinnedStationRepository
import io.github.agimaulana.radio.domain.api.repository.RadioStationRepository
import io.github.agimaulana.radio.infrastructure.repository.DataStoreCatalogStateRepository
import io.github.agimaulana.radio.infrastructure.repository.PinnedStationRepositoryImpl
import io.github.agimaulana.radio.infrastructure.repository.RadioStationRepositoryImpl

@Module
@InstallIn(SingletonComponent::class)
interface RepositoryModule {

    @Binds
    fun bindRadioStationRepository(impl: RadioStationRepositoryImpl): RadioStationRepository

    @Binds
    fun bindPinnedStationRepository(impl: PinnedStationRepositoryImpl): PinnedStationRepository

    @Binds
    fun bindCatalogStateRepository(impl: DataStoreCatalogStateRepository): CatalogStateRepository
}
