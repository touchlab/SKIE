package `tests`.`coroutines`.`flow`.`configuration`.`annotations`.`disabled`

import co.touchlab.skie.configuration.annotations.FlowInterop
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@FlowInterop.Disabled
fun foo(): Flow<Int> = flowOf(1, 2, 3)
