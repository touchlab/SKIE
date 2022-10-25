package `tests`.`default_arguments`.`global_functions`.`configuration`.`disabled`.`annotations`

import co.touchlab.skie.configuration.DefaultArgumentInterop

@DefaultArgumentInterop.Disabled
fun foo(i: Int = 0): Int = i
