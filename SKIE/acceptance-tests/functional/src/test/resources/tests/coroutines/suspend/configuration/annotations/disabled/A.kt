package `tests`.`coroutines`.`suspend`.`configuration`.`annotations`.`disabled`

import co.touchlab.skie.configuration.annotations.SuspendInterop
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds


class A {
    var result = 1

    @SuspendInterop.Disabled
    suspend fun foo(): Int {
        delay(500.milliseconds)
        result = 0
        return result
    }
}
