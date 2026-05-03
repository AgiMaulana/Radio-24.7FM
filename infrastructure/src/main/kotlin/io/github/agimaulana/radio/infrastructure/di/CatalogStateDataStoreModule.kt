package io.github.agimaulana.radio.infrastructure.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.agimaulana.radio.infrastructure.repository.DataStoreCatalogStateRepository
import javax.inject.Singleton

private val Context.catalogStateDataStore: DataStore<Preferences> by preferencesDataStore(
    name = DataStoreCatalogStateRepository.DATA_STORE_NAME,
)

@Module
@InstallIn(SingletonComponent::class)
object CatalogStateDataStoreModule {
    @Provides
    @Singleton
    fun provideCatalogStateDataStore(
        @ApplicationContext context: Context,
    ): DataStore<Preferences> {
        return context.catalogStateDataStore
    }
}
