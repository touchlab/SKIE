package `tests`.`functions`.`file_scope_conversion`.`global_functions`.`properties`.`var`.`flow`

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf

var foo: Flow<String> = emptyFlow()

fun flow(): Flow<Int> = flowOf(1, 2, 3)
