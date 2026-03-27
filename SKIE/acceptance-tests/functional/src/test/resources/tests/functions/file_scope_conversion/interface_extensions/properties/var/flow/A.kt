package `tests`.`functions`.`file_scope_conversion`.`interface_extensions`.`properties`.`var`.`flow`

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import tests.functions.file_scope_conversion.interface_extensions.I

private var fooStorage: Flow<String> = emptyFlow()

var I.foo: Flow<String>
    get() = fooStorage
    set(value) {
        fooStorage = value
    }

fun flow(): Flow<Int> = flowOf(1, 2, 3)
