package co.touchlab.skie.runtime.coroutines.flow

import kotlinx.coroutines.flow.SharedFlow

class SkieKotlinSharedFlow<out T : Any>(@param:ObjCName(swiftName = "_") private val delegate: SharedFlow<T>) : SharedFlow<T> by delegate
