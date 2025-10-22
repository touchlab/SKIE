package co.touchlab.skie.runtime.coroutines.flow

import kotlinx.coroutines.flow.Flow
import kotlin.experimental.ExperimentalObjCName

@OptIn(ExperimentalObjCName::class)
class SkieKotlinOptionalFlow<out T : Any>(@ObjCName(swiftName = "_") private val delegate: Flow<T?>) : Flow<T?> by delegate
