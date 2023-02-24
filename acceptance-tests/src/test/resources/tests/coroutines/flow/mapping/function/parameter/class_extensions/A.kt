package `tests`.`coroutines`.`flow`.`mapping`.`function`.`parameter`.`class_extensions`

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class A

fun A.foo(flow: Flow<String>) {
}

fun flow(): Flow<Int> = flowOf(1, 2, 3)
