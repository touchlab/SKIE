package co.touchlab.skie.runtime.coroutines.flow

import kotlinx.coroutines.flow.StateFlow

class SkieKotlinOptionalStateFlow<out T : Any>(@param:ObjCName(swiftName = "_") private val delegate: StateFlow<T?>) : StateFlow<T?> by delegate
