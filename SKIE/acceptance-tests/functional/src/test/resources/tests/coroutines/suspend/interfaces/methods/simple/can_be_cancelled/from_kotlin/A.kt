package `tests`.`coroutines`.`suspend`.`interfaces`.`methods`.`simple`.`can_be_cancelled`.`from_kotlin`

import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope

interface A {

    suspend fun foo(): Unit = coroutineScope {
        cancel()
    }
}

class A1 : A
