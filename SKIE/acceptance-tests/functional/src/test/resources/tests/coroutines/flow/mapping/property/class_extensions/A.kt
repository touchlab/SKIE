package `tests`.`coroutines`.`flow`.`mapping`.`property`.`class_extensions`

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class A

var A.foo: Flow<Int>
    get() = global
    set(value) {
        global = value
    }

val foo2: Flow<Int> = flowOf(1, 2, 3).map { it * 2 }

private var global: Flow<Int> = flowOf(1, 2, 3)
