package co.touchlab.skie.runtime.coroutines.flow

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.produceIn

class SkieColdFlowIterator<E>(
    flow: Flow<E>,
) {

    private val produceIn: CoroutineScope = CoroutineScope(Dispatchers.Default)

    private var next: Any? = Delimiter

    @Suppress("OPT_IN_USAGE")
    private val channel = flow {
        emit(Delimiter)

        flow.collect {
            emit(it)
            emit(Delimiter)
        }
    }
        .buffer(Channel.RENDEZVOUS)
        .produceIn(produceIn)

    private val iterator = channel.iterator()

    suspend operator fun hasNext(): Boolean {
        while (next == Delimiter && iterator.hasNext()) {
            next = iterator.next()
        }

        return next != Delimiter
    }

    @Suppress("UNCHECKED_CAST")
    operator fun next(): E {
        if (next == Delimiter) throw IllegalStateException("hasNext must be called (and return true) before it's possible to call next.")

        val result = next as E

        next = Delimiter

        return result
    }

    fun cancel() {
        channel.cancel()

        next = Delimiter
    }

    private object Delimiter
}
