package `tests`.`coroutines`.`flow`.`mapping`.`function`.`parameter`.`interface_members`

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

interface I {

    fun foo(flow: Flow<String>)
}

class A : I {

    override fun foo(flow: Flow<String>) {
    }
}

fun flow(): Flow<Int> = flowOf(1, 2, 3)
