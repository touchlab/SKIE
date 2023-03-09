package `tests`.`coroutines`.`flow`.`invariants`.`crash_on_kotlin_exception`

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

fun foo(): Flow<Int> = flow {
    emit(1)

    throw IllegalStateException("Exception from Kotlin")
}
