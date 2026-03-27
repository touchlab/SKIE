package `tests`.`coroutines`.`suspend`.`global_functions`.`throwing`.`can_be_called_from_background_thread`

import kotlinx.coroutines.CancellationException

@Throws(IllegalArgumentException::class, CancellationException::class)
suspend fun foo(): Int = 0
