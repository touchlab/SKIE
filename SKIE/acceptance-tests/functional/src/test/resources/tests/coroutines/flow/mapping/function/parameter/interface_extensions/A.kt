package `tests`.`coroutines`.`flow`.`mapping`.`function`.`parameter`.`interface_extensions`

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

interface I

class A : I

fun I.foo(flow: Flow<String>) {
}

fun flow(): Flow<Int> = flowOf(1, 2, 3)
