package `tests`.`coroutines`.`flow`.`mapping`.`property`.`interface_members`

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

interface I {

    var foo: Flow<Int>
}

class A : I {

    override var foo: Flow<Int>
        get() = global
        set(value) {
            global = value
        }
}

val foo2: Flow<Int> = flowOf(1, 2, 3).map { it * 2 }

private var global: Flow<Int> = flowOf(1, 2, 3)
