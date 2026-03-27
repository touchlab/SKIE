package `tests`.`coroutines`.`flow`.`mapping`.`types`.`nullable`

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

fun foo(): Flow<Int>? = flowOf(1, 2, 3)
