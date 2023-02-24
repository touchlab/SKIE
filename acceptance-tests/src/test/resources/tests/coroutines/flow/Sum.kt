package `tests`.`coroutines`.`flow`

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList

suspend fun sum(flow: Flow<Int>) = flow.toList().sum()

suspend fun sum(flow: Flow<Int?>) = flow.toList().filterNotNull().sum()
