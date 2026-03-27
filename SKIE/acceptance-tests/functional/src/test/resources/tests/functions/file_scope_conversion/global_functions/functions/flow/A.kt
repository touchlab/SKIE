package `tests`.`functions`.`file_scope_conversion`.`global_functions`.`functions`.`flow`

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

fun foo(flow: Flow<String>) {
}

fun flow(): Flow<Int> = flowOf(1, 2, 3)
