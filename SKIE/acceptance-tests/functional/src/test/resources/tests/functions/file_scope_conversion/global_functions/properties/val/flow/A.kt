package `tests`.`functions`.`file_scope_conversion`.`global_functions`.`properties`.`val`.`flow`

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

fun foo(flow: Flow<String>) {
}

val flow: Flow<Int> = flowOf(1, 2, 3)
