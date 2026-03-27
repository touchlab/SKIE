package `tests`.`coroutines`.`flow`.`mapping`.`function`.`return`.`class_members`

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class A {

    fun foo(): Flow<Int> = flowOf(1, 2, 3)
}
