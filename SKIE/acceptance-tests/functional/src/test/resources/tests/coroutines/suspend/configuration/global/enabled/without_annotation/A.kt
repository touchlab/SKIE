package `tests`.`coroutines`.`suspend`.`configuration`.`global`.`enabled`.`without_annotation`

import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

class A {

    var result = 0

    suspend fun foo(): Int {
        delay(500.milliseconds)
        result = 1
        return result
    }
}
