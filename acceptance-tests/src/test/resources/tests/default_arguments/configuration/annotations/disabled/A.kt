package `tests`.`default_arguments`.`configuration`.`annotations`.`disabled`

import co.touchlab.skie.configuration.DefaultArgumentInterop

@DefaultArgumentInterop.Disabled
fun foo(i: Int = 0): Int = i
