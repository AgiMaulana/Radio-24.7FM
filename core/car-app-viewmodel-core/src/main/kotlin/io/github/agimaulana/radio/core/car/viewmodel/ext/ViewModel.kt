package io.github.agimaulana.radio.core.car.viewmodel.ext

import androidx.car.app.Screen
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.github.agimaulana.radio.core.car.viewmodel.internal.getViewModelStoreOwner

/**
 * 3. The elegant delegate for standardizing ViewModel initialization in Car Apps.
 */
public inline fun <reified VM : ViewModel> Screen.viewModel(
    crossinline factoryProducer: () -> ViewModelProvider.Factory
): Lazy<VM> {
    return lazy(LazyThreadSafetyMode.NONE) {
        val owner = getViewModelStoreOwner()
        ViewModelProvider(owner, factoryProducer())[VM::class.java]
    }
}
