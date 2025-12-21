package io.github.agimaulana.radio.core.network.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.agimaulana.radio.core.network.RetrofitBuilderFactory
import io.github.agimaulana.radio.core.network.interceptor.SampleCustomInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    fun provideSampleCustomInterceptor(): SampleCustomInterceptor {
        return SampleCustomInterceptor()
    }


    @Provides
    fun provideRetrofit(): Retrofit {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
        return RetrofitBuilderFactory()
            .create(
                baseUrl = "https://de1.api.radio-browser.info/",
                okHttpClient = okHttpClient,
            )
            .build()
    }
}