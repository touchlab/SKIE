package `tests`.`coroutines`.`flow`.`mapping`.`function`.`return`.`class_extensions`

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class A

fun A.foo(): Flow<Int> = flowOf(1, 2, 3)
