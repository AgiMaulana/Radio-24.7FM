package io.github.agimaulana.radio.core.glance.viewmodel.hilt

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.hilt.android.EntryPointAccessors
import io.github.agimaulana.radio.core.glance.viewmodel.getViewModelStoreOwner

/**
 * Provide a Hilt-backed ViewModelProvider.Factory usable in Glance widgets.
 */
public fun provideHiltViewModelFactoryForGlance(context: Context): ViewModelProvider.Factory {
    val entryPoint = EntryPointAccessors.fromApplication(context, GlanceViewModelFactoryEntryPoint::class.java)
    return entryPoint.getViewModelFactory()
}

/**
 * Convenience extension mirroring the glanceViewModel lazy delegate but using Hilt factory.
 * Usage inside provideGlance(context, id):
 * val vm by hiltGlanceViewModel<MyViewModel>(id, context)
 */
public inline fun <reified VM : ViewModel> GlanceAppWidget.hiltGlanceViewModel(
    glanceId: GlanceId,
    context: Context
): Lazy<VM> {
    return lazy(LazyThreadSafetyMode.NONE) {
        val factory = provideHiltViewModelFactoryForGlance(context)
        ViewModelProvider(getViewModelStoreOwner(glanceId), factory)[VM::class.java]
    }
}
