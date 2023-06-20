package co.touchlab.skie.runtime.coroutines.flow

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlin.experimental.ExperimentalObjCName

@OptIn(ExperimentalObjCName::class)
class SkieKotlinMutableSharedFlow<T : Any>(
    @ObjCName(swiftName = "_") private val delegate: MutableSharedFlow<T>,
) : MutableSharedFlow<T> by delegate
