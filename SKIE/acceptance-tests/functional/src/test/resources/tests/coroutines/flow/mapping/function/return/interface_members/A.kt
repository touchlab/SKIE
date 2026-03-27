package `tests`.`coroutines`.`flow`.`mapping`.`function`.`return`.`interface_members`

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

interface I {

    fun foo(): Flow<Int>
}

class A : I {

    override fun foo(): Flow<Int> = flowOf(1, 2, 3)
}
