package `tests`.`coroutines`.`suspend`.`interfaces`.`extensions`.`simple`.`can_be_cancelled`.`from_kotlin`

import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope

interface A

class A1 : A

suspend fun A.foo(): Unit = coroutineScope {
    cancel()
}
