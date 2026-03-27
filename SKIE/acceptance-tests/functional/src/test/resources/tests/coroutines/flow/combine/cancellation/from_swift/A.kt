package `tests`.`coroutines`.`flow`.`combine`.`cancellation`.`from_swift`

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class A {
    var wasCancelled = false
    var lastEmitted: Int? = null

    fun flow(): Flow<Int> = flow {
        try {
            repeat(1_000_000_000) {
                try {
                    emit(it)
                    lastEmitted = it
                } catch (t: Throwable) {
                    println("Emit throwed $t")
                    throw t
                }
                delay(1)
            }
        } catch (e: CancellationException) {
            wasCancelled = true
            throw e
        }
    }
}
