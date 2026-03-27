package `tests`.`enums`.`renaming`.`disabled`.`camel_case`

import co.touchlab.skie.configuration.annotations.EnumInterop

@EnumInterop.LegacyCaseName.Enabled
enum class A {

    aa,
    aaBb,
    aaBbCc,
}

val a: A = A.aa

val index: Int = 0
