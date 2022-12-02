package `tests`.`coroutines`.`suspend`.`global_functions`.`throwing`.`crashes_on_undeclared_exception`

import kotlinx.coroutines.CancellationException

@Throws(IllegalArgumentException::class, CancellationException::class)
suspend fun foo(): Int = throw IllegalStateException("Undeclared exception")
