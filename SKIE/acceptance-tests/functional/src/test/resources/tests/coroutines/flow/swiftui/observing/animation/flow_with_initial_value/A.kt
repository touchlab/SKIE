package `tests`.`coroutines`.`flow`.`swiftui`.`observing`.`animation`.`flow_with_initial_value`

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

fun ticking(): Flow<Int> = flowOf(1, 2, 3)
