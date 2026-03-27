package `tests`.`coroutines`.`suspend`.`classes`.`extensions`.`simple`.`can_be_cancelled`.`from_kotlin`

import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope

class A

suspend fun A.foo(): Unit = coroutineScope {
    cancel()
}
