package `tests`.`functions`.`renaming`.`disabled`

import co.touchlab.skie.configuration.annotations.FunctionInterop

fun foo(a: Int): Int = 0

@FunctionInterop.LegacyName.Enabled
fun foo(a: String): Int = 1
