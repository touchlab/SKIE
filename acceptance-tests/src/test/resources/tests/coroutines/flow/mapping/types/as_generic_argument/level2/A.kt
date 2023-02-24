package `tests`.`coroutines`.`flow`.`mapping`.`types`.`as_generic_argument`.`level2`

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList

fun foo(): Pair<Pair<Flow<Int>, Unit>, Unit> = flowOf(1, 2, 3) to Unit to Unit
