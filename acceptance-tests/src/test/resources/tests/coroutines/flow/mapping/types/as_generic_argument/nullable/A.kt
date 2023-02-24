package `tests`.`coroutines`.`flow`.`mapping`.`types`.`as_generic_argument`.`nullable`

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList

fun foo(): Pair<Flow<Int>?, Unit> = flowOf(1, 2, 3) to Unit
