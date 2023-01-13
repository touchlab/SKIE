package `tests`.`coroutines`.`suspend`.`configuration`.`global`.`enabled`.`overriden_by_annotation`

import co.touchlab.skie.configuration.annotations.SuspendInterop

class A {

    @SuspendInterop.Disabled
    suspend fun foo(): Int = 0
}
