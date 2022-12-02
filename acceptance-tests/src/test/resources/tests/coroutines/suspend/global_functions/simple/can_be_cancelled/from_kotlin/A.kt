package `tests`.`coroutines`.`suspend`.`global_functions`.`simple`.`can_be_cancelled`.`from_kotlin`

import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope

suspend fun foo(): Unit = coroutineScope {
    cancel()
}
