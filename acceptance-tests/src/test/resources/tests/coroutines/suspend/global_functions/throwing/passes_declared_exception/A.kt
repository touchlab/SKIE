package `tests`.`coroutines`.`suspend`.`global_functions`.`throwing`.`passes_declared_exception`

import kotlinx.coroutines.CancellationException

@Throws(IllegalArgumentException::class, IllegalStateException::class, CancellationException::class)
suspend fun foo(): Int = throw IllegalStateException("Declared exception")
