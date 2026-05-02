package io.github.agimaulana.radio.core.car.viewmodel.internal

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner

/**
 * 1. An isolated ViewModelStoreOwner that clears itself when the screen is destroyed.
 */
internal class CarScreenViewModelStoreOwner(lifecycle: Lifecycle) : ViewModelStoreOwner {
    private val _viewModelStore = ViewModelStore()

    override fun getViewModelStore(): ViewModelStore = _viewModelStore

    init {
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                _viewModelStore.clear()
            }
        })
    }
}