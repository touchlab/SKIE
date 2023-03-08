package `tests`.`coroutines`.`flow`.`mapping`.`types`.`as_generic_argument`.`nullable`

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class A<T>(val value: T)

fun foo(): A<Flow<Int>?> = A(flowOf(1, 2, 3))
