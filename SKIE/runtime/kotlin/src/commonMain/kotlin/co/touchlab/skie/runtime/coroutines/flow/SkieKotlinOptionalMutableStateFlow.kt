package co.touchlab.skie.runtime.coroutines.flow

import kotlinx.coroutines.flow.MutableStateFlow

class SkieKotlinOptionalMutableStateFlow<T : Any>(
    @param:ObjCName(swiftName = "_") private val delegate: MutableStateFlow<T?>,
) : MutableStateFlow<T?> by delegate
