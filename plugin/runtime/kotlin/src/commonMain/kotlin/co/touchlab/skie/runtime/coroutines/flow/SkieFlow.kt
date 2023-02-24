package co.touchlab.skie.runtime.coroutines.flow

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector

class SkieFlow<out T : Any>(private val delegate: Flow<T>) : Flow<T> {

    override suspend fun collect(collector: FlowCollector<T>) {
        delegate.collect(collector)
    }
}
