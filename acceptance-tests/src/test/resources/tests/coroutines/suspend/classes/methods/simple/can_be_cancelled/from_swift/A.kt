package `tests`.`coroutines`.`suspend`.`classes`.`methods`.`simple`.`can_be_cancelled`.`from_swift`

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay

class A {

    suspend fun foo(): Int {
        try {
            delay(1000)
        } catch (_: CancellationException) {
            return 0
        }

        return 1
    }
}
