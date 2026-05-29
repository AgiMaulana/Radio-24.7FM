package io.github.agimaulana.radio.core.glance.viewmodel.ext

import androidx.compose.runtime.Composable
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.github.agimaulana.radio.core.glance.viewmodel.internal.clearViewModelStore
import io.github.agimaulana.radio.core.glance.viewmodel.internal.getOrCreateViewModelStoreOwner

public inline fun <reified VM : ViewModel> GlanceAppWidget.glanceViewModel(
    glanceId: GlanceId,
    crossinline factory: () -> ViewModelProvider.Factory,
): Lazy<VM> {
    return lazy(LazyThreadSafetyMode.NONE) {
        val owner = getOrCreateViewModelStoreOwner(glanceId)
        ViewModelProvider(owner, factory())[VM::class.java]
    }
}

@Composable
public inline fun <reified VM : ViewModel> glanceViewModel(
    glanceId: GlanceId,
    crossinline factory: () -> ViewModelProvider.Factory,
): VM {
    val owner = getOrCreateViewModelStoreOwner(glanceId)
    return ViewModelProvider(owner, factory())[VM::class.java]
}

public fun clearViewModels(glanceId: GlanceId) {
    clearViewModelStore(glanceId)
}
