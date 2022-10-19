package `tests`.`default_arguments`.`configuration`.`global`.`enabled`.`overriden_by_annotation`

import co.touchlab.swiftgen.api.DefaultArgumentInterop

@DefaultArgumentInterop.Disabled
fun foo(i: Int = 0): Int = i
