// ViewModelKey.kt
package io.github.agimaulana.radio.core.car.viewmodel.hilt

import androidx.lifecycle.ViewModel
import dagger.MapKey
import kotlin.reflect.KClass

@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER
)
@Retention(AnnotationRetention.RUNTIME)
@MapKey
public annotation class ViewModelKey(val value: KClass<out ViewModel>)
