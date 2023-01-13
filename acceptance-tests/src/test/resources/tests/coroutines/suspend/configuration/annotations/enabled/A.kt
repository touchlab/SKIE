package `tests`.`coroutines`.`suspend`.`configuration`.`annotations`.`enabled`

import co.touchlab.skie.configuration.annotations.SuspendInterop

class A {

    @SuspendInterop.Enabled
    suspend fun foo(): Int = 0
}
