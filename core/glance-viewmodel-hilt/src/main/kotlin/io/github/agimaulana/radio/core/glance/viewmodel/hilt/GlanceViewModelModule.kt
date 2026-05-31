package io.github.agimaulana.radio.core.glance.viewmodel.hilt

import androidx.lifecycle.ViewModel
import dagger.Module
import dagger.hilt.InstallIn
import dagger.multibindings.Multibinds
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class GlanceViewModelModule {

    @Multibinds
    abstract fun viewModelMap(): Map<Class<out ViewModel>, ViewModel>
}
