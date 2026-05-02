package io.github.agimaulana.radio.core.car.viewmodel.hilt.ext

import androidx.car.app.Screen
import androidx.lifecycle.ViewModel
import dagger.hilt.android.EntryPointAccessors
import io.github.agimaulana.radio.core.car.viewmodel.ext.viewModel
import io.github.agimaulana.radio.core.car.viewmodel.hilt.CarViewModelFactoryEntryPoint

public inline fun <reified VM : ViewModel> Screen.viewModel(): Lazy<VM> {

    // We simply call the Core module's delegate!
    return this.viewModel<VM> {

        // This block is the 'factoryProducer' lambda.
        // It runs safely inside the core's Lazy block.
        val entryPoint = EntryPointAccessors.fromApplication(
            carContext.applicationContext,
            CarViewModelFactoryEntryPoint::class.java
        )
        entryPoint.getViewModelFactory()
    }
}

