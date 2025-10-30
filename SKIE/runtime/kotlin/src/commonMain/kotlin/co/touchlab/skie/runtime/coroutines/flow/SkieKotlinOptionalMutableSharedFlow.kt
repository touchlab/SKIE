package co.touchlab.skie.runtime.coroutines.flow

import kotlinx.coroutines.flow.MutableSharedFlow

class SkieKotlinOptionalMutableSharedFlow<T : Any>(
    @param:ObjCName(swiftName = "_") private val delegate: MutableSharedFlow<T?>,
) : MutableSharedFlow<T?> by delegate
