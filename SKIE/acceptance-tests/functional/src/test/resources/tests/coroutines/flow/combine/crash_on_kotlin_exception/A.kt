package `tests`.`coroutines`.`flow`.`combine`.`crash_on_kotlin_exception`

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

fun foo(): Flow<Int> = flow {
    emit(1)

    throw IllegalStateException("Exception from Kotlin")
}
