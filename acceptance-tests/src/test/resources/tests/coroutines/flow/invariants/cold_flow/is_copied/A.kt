package tests.coroutines.flow.invariants.cold_flow.is_copied

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

fun flow(): Flow<Int> = flow {
    emit(1)
    emit(2)
    emit(3)
}
