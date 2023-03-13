package co.touchlab.skie.runtime.coroutines.flow

import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.experimental.ExperimentalObjCName

@OptIn(ExperimentalObjCName::class)
class SkieKotlinOptionalMutableStateFlow<T : Any>(
    @ObjCName(swiftName = "_") private val delegate: MutableStateFlow<T?>,
) : MutableStateFlow<T?> by delegate
