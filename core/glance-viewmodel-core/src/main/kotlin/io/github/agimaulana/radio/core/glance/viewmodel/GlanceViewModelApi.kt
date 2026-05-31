package io.github.agimaulana.radio.core.glance.viewmodel

import androidx.glance.GlanceId
import androidx.lifecycle.ViewModelStoreOwner
import io.github.agimaulana.radio.core.glance.viewmodel.internal.getOrCreateViewModelStoreOwner

/**
 * Public helper to get the ViewModelStoreOwner for a GlanceId.
 * This is a small, explicit bridge so other modules (hilt) can reuse the store owner.
 */
public fun getViewModelStoreOwner(glanceId: GlanceId): ViewModelStoreOwner {
    return getOrCreateViewModelStoreOwner(glanceId)
}
