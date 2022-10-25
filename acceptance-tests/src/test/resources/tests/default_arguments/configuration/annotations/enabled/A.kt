package `tests`.`default_arguments`.`configuration`.`annotations`.`enabled`

import co.touchlab.skie.configuration.DefaultArgumentInterop

@DefaultArgumentInterop.Enabled
fun foo(i: Int = 0): Int = i
