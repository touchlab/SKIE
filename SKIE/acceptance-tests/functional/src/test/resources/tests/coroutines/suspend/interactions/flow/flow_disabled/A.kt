package `tests`.`coroutines`.`suspend`.`interactions`.`flow`.`flow_disabled`

import co.touchlab.skie.configuration.annotations.FlowInterop
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList

@FlowInterop.Disabled
suspend fun foo(flow: Flow<Int>): Int =
    flow.toList().sum()

@FlowInterop.Disabled
fun flow(): Flow<Int> = flowOf(1, 2, 3)
