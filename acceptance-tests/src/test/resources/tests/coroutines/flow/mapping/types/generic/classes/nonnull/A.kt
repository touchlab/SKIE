package `tests`.`coroutines`.`flow`.`mapping`.`types`.`generic`.`classes`

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class A<T : Any> {

    @Suppress("UNCHECKED_CAST")
    fun foo(): Flow<T> = flowOf(1, 2, 3) as Flow<T>
}
