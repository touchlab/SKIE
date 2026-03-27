package `tests`.`bugs`.`returning_flow_from_suspend_overload`

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

suspend fun foo(): StateFlow<Int> = MutableStateFlow(1)
