package `tests`.`enums`.`renaming`.`disabled`.`upper_snake_case`

import co.touchlab.skie.configuration.annotations.EnumInterop

@EnumInterop.LegacyCaseName.Enabled
enum class A {

    AA,
    AA_BB,
    AA_BB_CC,
}

val a: A = A.AA

val index: Int = 0
