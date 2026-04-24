package io.github.agimaulana.radio.core.tracker.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.agimaulana.radio.core.tracker.EventTracker
import io.github.agimaulana.radio.core.tracker.internal.SentryEventTracker

@Module
@InstallIn(SingletonComponent::class)
internal interface TrackerModule {

    @Binds
    fun bindEventTracker(impl: SentryEventTracker): EventTracker
}
