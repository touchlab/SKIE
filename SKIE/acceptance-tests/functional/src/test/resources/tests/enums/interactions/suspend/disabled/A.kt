package `tests`.`enums`.`interactions`.`suspend`.`disabled`

import co.touchlab.skie.configuration.annotations.SuspendInterop

enum class A {
    A1, A2;

    @SuspendInterop.Disabled
    suspend fun foo(): Int = 0
}
