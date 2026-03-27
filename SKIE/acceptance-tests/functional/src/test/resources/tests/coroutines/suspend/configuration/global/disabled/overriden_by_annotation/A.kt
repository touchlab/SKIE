package `tests`.`coroutines`.`suspend`.`configuration`.`global`.`disabled`.`overriden_by_annotation`

import co.touchlab.skie.configuration.annotations.SuspendInterop
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

class A {

    var result = 0

    @SuspendInterop.Enabled
    suspend fun foo(): Int {
        delay(500.milliseconds)
        result = 1
        return result
    }
}
