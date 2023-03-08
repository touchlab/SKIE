package co.touchlab.skie.runtime.coroutines.flow

import kotlinx.coroutines.flow.Flow

class SkieKotlinFlow<out T : Any>(private val delegate: Flow<T>) : Flow<T> by delegate
