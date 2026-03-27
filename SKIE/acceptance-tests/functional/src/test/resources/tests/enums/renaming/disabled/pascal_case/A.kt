package `tests`.`enums`.`renaming`.`disabled`.`pascal_case`

import co.touchlab.skie.configuration.annotations.EnumInterop

@EnumInterop.LegacyCaseName.Enabled
enum class A {

    Aa,
    AaBb,
    AaBbCc,
}

val a: A = A.Aa

val index: Int = 0
