package io.github.agimaulana.radio.core.car.viewmodel.internal

import androidx.car.app.Screen
import androidx.lifecycle.ViewModelStoreOwner
import java.util.WeakHashMap

/**
 * 2. A registry to safely tie StoreOwners to Screen instances without memory leaks.
 */
private val screenStores = WeakHashMap<Screen, CarScreenViewModelStoreOwner>()

@PublishedApi
internal fun Screen.getViewModelStoreOwner(): ViewModelStoreOwner {
    return screenStores.getOrPut(this) {
        CarScreenViewModelStoreOwner(this.lifecycle)
    }
}
