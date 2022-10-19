package `tests`.`default_arguments`.`global_functions`.`configuration`.`disabled`.`annotations`

import co.touchlab.swiftgen.api.DefaultArgumentInterop

@DefaultArgumentInterop.Disabled
fun foo(i: Int = 0): Int = i
