package `tests`.`coroutines`.`flow`.`configuration`.`global`.`enabled`.`without_annotation`

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

fun foo(): Flow<Int> = flowOf(1, 2, 3)
