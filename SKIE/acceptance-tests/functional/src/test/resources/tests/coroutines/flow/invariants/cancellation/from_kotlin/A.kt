package `tests`.`coroutines`.`flow`.`invariants`.`cancellation`.`from_kotlin`

import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

fun flow(): Flow<Int> = flow {
    delay(1)
    emit(1)

    delay(1)
    emit(2)

    delay(1)
    emit(3)

    currentCoroutineContext().cancel()

    delay(1)
    emit(4)
}
