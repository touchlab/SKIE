package `tests`.`coroutines`.`flow`.`swiftui`.`observing`.`animation`.`multiple_flows`

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

val counter1: StateFlow<Int> = MutableStateFlow(0)
val counter2: StateFlow<Int> = MutableStateFlow(0)
