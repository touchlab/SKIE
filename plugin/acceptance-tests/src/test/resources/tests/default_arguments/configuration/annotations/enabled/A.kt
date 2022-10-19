package `tests`.`default_arguments`.`configuration`.`annotations`.`enabled`

import co.touchlab.swiftgen.api.DefaultArgumentInterop

@DefaultArgumentInterop.Enabled
fun foo(i: Int = 0): Int = i
