@file:Suppress("PackageDirectoryMismatch")

package co.touchlab.skie.runtime.coroutines.flow

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector

class SkieFlow<T>(delegate: Flow<T>) : Flow<T> {

    override suspend fun collect(collector: FlowCollector<T>) {
        TODO("Not yet implemented")
    }
}
