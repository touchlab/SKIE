package `tests`.`default_arguments`.`configuration`.`annotations`.`disabled`

import co.touchlab.skie.configuration.annotations.DefaultArgumentInterop

@DefaultArgumentInterop.Disabled
fun foo(i: Int = 0): Int = i
