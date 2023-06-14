package co.touchlab.skie.runtime.coroutines.flow

import kotlinx.coroutines.flow.StateFlow
import kotlin.experimental.ExperimentalObjCName

@OptIn(ExperimentalObjCName::class)
class SkieKotlinStateFlow<out T : Any>(@ObjCName(swiftName = "_") private val delegate: StateFlow<T>) : StateFlow<T> by delegate
