package `tests`.`coroutines`.`suspend`.`global_functions`.`throwing`.`converts_cancellation_exception`.`explicitly_declared`

import kotlinx.coroutines.CancellationException

@Throws(IllegalArgumentException::class, CancellationException::class)
suspend fun foo(): Int = throw CancellationException()
