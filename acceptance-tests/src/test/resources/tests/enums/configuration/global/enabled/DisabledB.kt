package `tests`.`enums`.`configuration`.`global`.`enabled`

import co.touchlab.skie.configuration.EnumInterop

@EnumInterop.Disabled
enum class B {
    B1,
    B2,
}

fun b1(): B {
    return B.B1
}
