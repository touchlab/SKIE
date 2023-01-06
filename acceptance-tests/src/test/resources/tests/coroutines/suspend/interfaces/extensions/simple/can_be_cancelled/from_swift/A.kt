package `tests`.`coroutines`.`suspend`.`interfaces`.`extensions`.`simple`.`can_be_cancelled`.`from_swift`

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay

interface A

class A1 : A

suspend fun A.foo(): Int {
    try {
        delay(1000)
    } catch (_: CancellationException) {
        return 0
    }

    return 1
}
