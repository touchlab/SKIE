package `tests`.`coroutines`.`suspend`.`configuration`.`annotations`.`disabled`

import co.touchlab.skie.configuration.annotations.SuspendInterop

class A {

    @SuspendInterop.Disabled
    suspend fun foo(): Int = 0
}
