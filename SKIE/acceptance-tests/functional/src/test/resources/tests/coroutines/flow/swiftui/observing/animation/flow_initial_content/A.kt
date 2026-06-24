package `tests`.`coroutines`.`flow`.`swiftui`.`observing`.`animation`.`flow_initial_content`

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

fun items(): Flow<Int> = flowOf(1, 2, 3)
