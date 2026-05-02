// CarAppViewModelModule.kt
package io.github.agimaulana.radio.feature.auto.di

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import io.github.agimaulana.radio.core.car.viewmodel.hilt.ViewModelKey
import io.github.agimaulana.radio.feature.auto.screen.main.MainScreenViewModel

// Example module - Add your ViewModels here as you build them
@Module
@InstallIn(SingletonComponent::class)
abstract class CarAppViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(MainScreenViewModel::class)
    abstract fun bindRadioScreenViewModel(viewModel: MainScreenViewModel): ViewModel
}