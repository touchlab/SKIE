package co.touchlab.skie.runtime.coroutines.flow

import kotlinx.coroutines.flow.SharedFlow
import kotlin.experimental.ExperimentalObjCName

@OptIn(ExperimentalObjCName::class)
class SkieKotlinSharedFlow<out T : Any>(@ObjCName(swiftName = "_") private val delegate: SharedFlow<T>) : SharedFlow<T> by delegate
