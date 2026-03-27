package `tests`.`coroutines`.`flow`.`mapping`.`function`.`return`.`interface_extensions`

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

interface I

class A : I

fun I.foo(): Flow<Int> = flowOf(1, 2, 3)
