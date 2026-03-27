package `tests`.`coroutines`.`flow`.`invariants`.`cold_flow`.`starts_after_collect`

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

var emittedElements = 0

fun flow(): Flow<Int> = flow {
    emittedElements++
    emit(1)
    delay(1)

    emittedElements++
    emit(2)
    delay(1)

    emittedElements++
    emit(3)
    delay(1)
}
