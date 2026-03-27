package `tests`.`enums`.`renaming`.`disabled`.`mix`

import co.touchlab.skie.configuration.annotations.EnumInterop

@EnumInterop.LegacyCaseName.Enabled
enum class A {

    AA__BB__CC,
    Aa_BB,
    aa_Bb_CC,
}

val a: A = A.AA__BB__CC

val index: Int = 0
