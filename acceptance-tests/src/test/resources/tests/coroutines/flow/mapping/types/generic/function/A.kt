package `tests`.`coroutines`.`flow`.`mapping`.`types`.`generic`.`function`

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@Suppress("UNCHECKED_CAST", "FINAL_UPPER_BOUND")
fun <T : Int> foo(): Flow<T> = flowOf(1, 2, 3) as Flow<T>
