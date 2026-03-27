package `tests`.`functions`.`file_scope_conversion`.`interface_extensions`.`properties`.`val`.`flow`

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import tests.functions.file_scope_conversion.interface_extensions.I

fun foo(flow: Flow<String>) {
}

val I.flow: Flow<Int>
    get() = flowOf(1, 2, 3)
