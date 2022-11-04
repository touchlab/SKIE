package `tests`.`enums`.`configuration`.`global`.`disabled`

import co.touchlab.skie.configuration.annotations.EnumInterop

@EnumInterop.Enabled
enum class A {
    A1,
    A2,
}

fun a1(): A {
    return A.A1
}
