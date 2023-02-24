package `tests`.`coroutines`.`flow`.`mapping`.`types`.`lambda`.`parameter`

import co.touchlab.skie.runtime.coroutines.flow.SkieFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

fun foo(): (Flow<Int>) -> Int = { 6 }

fun flow(): Flow<Int> = flowOf(1, 2, 3)
