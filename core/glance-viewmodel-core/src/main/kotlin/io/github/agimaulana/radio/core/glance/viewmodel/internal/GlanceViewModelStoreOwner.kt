package io.github.agimaulana.radio.core.glance.viewmodel.internal

import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner

internal class GlanceViewModelStoreOwner : ViewModelStoreOwner {
    val store = ViewModelStore()

    override fun getViewModelStore(): ViewModelStore = store
}
