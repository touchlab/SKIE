package `tests`.`default_arguments`.`configuration`.`global`.`disabled`.`overriden_by_annotation`

import co.touchlab.skie.configuration.annotations.DefaultArgumentInterop

@DefaultArgumentInterop.Enabled
fun foo(i: Int = 0): Int = i
