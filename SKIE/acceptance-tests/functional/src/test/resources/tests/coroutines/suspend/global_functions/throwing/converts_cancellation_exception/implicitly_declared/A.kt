package `tests`.`coroutines`.`suspend`.`global_functions`.`throwing`.`converts_cancellation_exception`.`implicitly_declared`

import kotlinx.coroutines.CancellationException

suspend fun foo(): Int = throw CancellationException()
