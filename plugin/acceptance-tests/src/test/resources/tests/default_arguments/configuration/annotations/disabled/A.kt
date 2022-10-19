package `tests`.`default_arguments`.`configuration`.`annotations`.`disabled`

import co.touchlab.swiftgen.api.DefaultArgumentInterop

@DefaultArgumentInterop.Disabled
fun foo(i: Int = 0): Int = i
