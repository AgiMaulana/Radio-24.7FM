package io.github.agimaulana.radio.core.glance.state

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
public object GlanceStateModule {

    @Provides
    @Singleton
    @ApplicationCoroutineScope
    public fun provideApplicationScope(): CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
}
