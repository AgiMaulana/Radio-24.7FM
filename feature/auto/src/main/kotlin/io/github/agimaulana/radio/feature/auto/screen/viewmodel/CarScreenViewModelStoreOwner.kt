// CarScreenExtensions.kt
package io.github.agimaulana.radio.feature.auto.screen.viewmodel

import androidx.car.app.Screen
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import java.util.WeakHashMap

/**
 * 1. An isolated ViewModelStoreOwner that clears itself when the screen is destroyed.
 */
class CarScreenViewModelStoreOwner(lifecycle: Lifecycle) : ViewModelStoreOwner {
    private val _viewModelStore = ViewModelStore()

    override fun getViewModelStore() = _viewModelStore

    init {
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                _viewModelStore.clear()
            }
        })
    }
}

/**
 * 2. A registry to safely tie StoreOwners to Screen instances without memory leaks.
 */
private val screenStores = WeakHashMap<Screen, CarScreenViewModelStoreOwner>()

fun Screen.getViewModelStoreOwner(): ViewModelStoreOwner {
    return screenStores.getOrPut(this) {
        CarScreenViewModelStoreOwner(this.lifecycle)
    }
}

/**
 * 3. The elegant delegate for standardizing ViewModel initialization in Car Apps.
 */
inline fun <reified VM : ViewModel> Screen.viewModel(
    factory: ViewModelProvider.Factory
): Lazy<VM> {
    return lazy(LazyThreadSafetyMode.NONE) {
        val owner = getViewModelStoreOwner()
        ViewModelProvider(owner, factory)[VM::class.java]
    }
}