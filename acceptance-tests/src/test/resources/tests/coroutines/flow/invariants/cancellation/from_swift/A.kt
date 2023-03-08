package tests.coroutines.flow.invariants.cancellation.from_swift

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

var wasCancelled = false

fun flow(): Flow<Int> = flow {
    try {
        repeat(1_000_000_000) {
            emit(it)
            delay(1)
        }
    } catch (e: CancellationException) {
        wasCancelled = true
        throw e
    }
}
