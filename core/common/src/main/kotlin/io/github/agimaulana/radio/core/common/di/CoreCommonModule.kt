package io.github.agimaulana.radio.core.common.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.agimaulana.radio.core.common.DateTimeProvider
import io.github.agimaulana.radio.core.common.DispatcherProvider
import io.github.agimaulana.radio.core.common.internal.DateTimeProviderImpl
import io.github.agimaulana.radio.core.common.internal.DefaultDispatcherProvider

@Module
@InstallIn(SingletonComponent::class)
object CoreCommonModule {

    @Provides
    fun provideDateTimeProvider(): DateTimeProvider = DateTimeProviderImpl()

    @Provides
    fun provideDispatcherProvider(): DispatcherProvider = DefaultDispatcherProvider()
}
