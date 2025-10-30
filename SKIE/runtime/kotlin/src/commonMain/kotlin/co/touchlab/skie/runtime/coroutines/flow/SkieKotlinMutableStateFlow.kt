package co.touchlab.skie.runtime.coroutines.flow

import kotlinx.coroutines.flow.MutableStateFlow

class SkieKotlinMutableStateFlow<T : Any>(
    @param:ObjCName(swiftName = "_") private val delegate: MutableStateFlow<T>,
) : MutableStateFlow<T> by delegate
