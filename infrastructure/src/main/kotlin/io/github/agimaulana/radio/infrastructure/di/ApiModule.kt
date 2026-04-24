package io.github.agimaulana.radio.infrastructure.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.agimaulana.radio.infrastructure.api.RadioStationApi
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
class ApiModule {

    @Provides
    fun provideRadioStationApi(retrofit: Retrofit): RadioStationApi {
        return retrofit.create(RadioStationApi::class.java)
    }
}
