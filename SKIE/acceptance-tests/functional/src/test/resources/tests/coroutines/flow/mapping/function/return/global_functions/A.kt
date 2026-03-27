package `tests`.`coroutines`.`flow`.`mapping`.`function`.`return`.`global_functions`

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

fun foo(): Flow<Int> = flowOf(1, 2, 3)
