package `tests`.`coroutines`.`suspend`.`interfaces`.`methods`.`simple`.`can_be_cancelled`.`from_swift`

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay

interface A {

    suspend fun foo(): Int {
        try {
            delay(1000)
        } catch (_: CancellationException) {
            return 0
        }

        return 1
    }
}

class A1 : A
