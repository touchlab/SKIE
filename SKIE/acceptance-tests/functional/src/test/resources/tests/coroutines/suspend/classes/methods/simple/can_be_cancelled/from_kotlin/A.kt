package `tests`.`coroutines`.`suspend`.`classes`.`methods`.`simple`.`can_be_cancelled`.`from_kotlin`

import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope

class A {

    suspend fun foo(): Unit = coroutineScope {
        cancel()
    }
}
