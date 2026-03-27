package `tests`.`coroutines`.`suspend`.`objects`.`extensions`.`simple`.`can_be_cancelled`.`from_kotlin`

import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope

object A

suspend fun A.foo(): Unit = coroutineScope {
    cancel()
}
