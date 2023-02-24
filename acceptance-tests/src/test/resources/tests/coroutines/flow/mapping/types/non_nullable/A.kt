package `tests`.`coroutines`.`flow`.`mapping`.`types`.`non_nullable`

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList

fun foo(): Flow<Int> = flowOf(1, 2, 3)
