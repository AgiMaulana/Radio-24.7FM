package io.github.agimaulana.radio.feature.widget

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import io.github.agimaulana.radio.core.glance.viewmodel.hilt.ViewModelKey

@Module
@InstallIn(SingletonComponent::class)
internal abstract class WidgetModule {

    @Binds
    @IntoMap
    @ViewModelKey(WidgetViewModel::class)
    abstract fun bindWidgetViewModel(viewModel: WidgetViewModel): ViewModel
}
