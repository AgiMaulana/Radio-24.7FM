package io.github.agimaulana.radio.core.glance.viewmodel.internal

import androidx.glance.GlanceId
import java.util.HashMap

private val stores = HashMap<String, GlanceViewModelStoreOwner>()

@PublishedApi
internal fun getOrCreateViewModelStoreOwner(glanceId: GlanceId): GlanceViewModelStoreOwner {
    return stores.getOrPut(glanceId.toString()) { GlanceViewModelStoreOwner() }
}

internal fun clearViewModelStore(glanceId: GlanceId) {
    stores.remove(glanceId.toString())?.store?.clear()
}
