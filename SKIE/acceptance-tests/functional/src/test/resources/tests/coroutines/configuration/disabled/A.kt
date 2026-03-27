package `tests`.`coroutines`.`configuration`.`disabled`

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

fun foo(): Flow<Int> = flow { }
