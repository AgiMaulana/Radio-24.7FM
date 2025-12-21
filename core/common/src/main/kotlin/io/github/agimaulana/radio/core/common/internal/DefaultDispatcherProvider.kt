package io.github.agimaulana.radio.core.common.internal

import io.github.agimaulana.radio.core.common.DispatcherProvider

internal class DefaultDispatcherProvider : DispatcherProvider {

    override val main = kotlinx.coroutines.Dispatchers.Main

    override val io = kotlinx.coroutines.Dispatchers.IO

    override val default = kotlinx.coroutines.Dispatchers.Default

    override val unconfined = kotlinx.coroutines.Dispatchers.Unconfined
}