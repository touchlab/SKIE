package `tests`.`functions`.`file_scope_conversion`.`interface_extensions`.`functions`.`flow`

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import tests.functions.file_scope_conversion.interface_extensions.I

fun I.foo(flow: Flow<String>) {
}

fun flow(): Flow<Int> = flowOf(1, 2, 3)
