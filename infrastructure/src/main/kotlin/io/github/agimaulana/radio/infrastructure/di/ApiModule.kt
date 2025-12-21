package io.github.agimaulana.radio.infrastructure.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.agimaulana.radio.core.network.RetrofitBuilderFactory
import io.github.agimaulana.radio.infrastructure.api.RadioStationApi
import io.github.agimaulana.radio.infrastructure.api.SampleUserApi
import okhttp3.OkHttpClient
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
class ApiModule {

    @Provides
    fun provideSampleUserApi(retrofit: Retrofit): SampleUserApi {
        return retrofit.create(SampleUserApi::class.java)
    }

    @Provides
    fun provideRadioStationApi(retrofit: Retrofit): RadioStationApi {
        return retrofit.create(RadioStationApi::class.java)
    }
}