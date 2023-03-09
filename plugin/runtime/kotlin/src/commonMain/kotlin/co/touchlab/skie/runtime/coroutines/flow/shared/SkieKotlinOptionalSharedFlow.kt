package co.touchlab.skie.runtime.coroutines.flow.shared

import kotlinx.coroutines.flow.SharedFlow
import kotlin.experimental.ExperimentalObjCName

@OptIn(ExperimentalObjCName::class)
class SkieKotlinOptionalSharedFlow<out T : Any>(@ObjCName(swiftName = "_") private val delegate: SharedFlow<T?>) : SharedFlow<T?> by delegate
