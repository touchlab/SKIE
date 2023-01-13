package `tests`.`coroutines`.`suspend`.`configuration`.`global`.`disabled`.`overriden_by_annotation`

import co.touchlab.skie.configuration.annotations.SuspendInterop

class A {

    @SuspendInterop.Enabled
    suspend fun foo(): Int = 0
}
