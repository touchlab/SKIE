package `tests`.`coroutines`.`flow`

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList

suspend fun sum(flow: Flow<Int>, max: Int? = null) = (if (max != null) flow.take(max) else flow).toList().sum()

suspend fun sum(flow: Flow<Int?>, max: Int? = null) = (if (max != null) flow.take(max) else flow).toList().filterNotNull().sum()
