package `tests`.`coroutines`.`suspend`.`configuration`.`global`.`disabled`.`without_annotation`

import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds


class A {
    var result = 1

    suspend fun foo(): Int {
        delay(500.milliseconds)
        result = 0
        return result
    }
}
