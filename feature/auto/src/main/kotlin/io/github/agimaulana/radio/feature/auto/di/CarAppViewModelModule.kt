// CarAppViewModelModule.kt
package io.github.agimaulana.radio.feature.auto.di

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap

// Example module - Add your ViewModels here as you build them
@Module
@InstallIn(SingletonComponent::class)
abstract class CarAppViewModelModule {

    /*
    @Binds
    @IntoMap
    @ViewModelKey(RadioScreenViewModel::class)
    abstract fun bindRadioScreenViewModel(viewModel: RadioScreenViewModel): ViewModel
    */
}