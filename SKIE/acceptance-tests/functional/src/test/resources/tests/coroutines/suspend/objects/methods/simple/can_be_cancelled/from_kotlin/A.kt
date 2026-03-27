package `tests`.`coroutines`.`suspend`.`objects`.`methods`.`simple`.`can_be_cancelled`.`from_kotlin`

import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope

object A {

    suspend fun foo(): Unit = coroutineScope {
        cancel()
    }
}
