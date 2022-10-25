package `tests`.`default_arguments`.`configuration`.`annotations`.`maximum_default_argument_count`.`above_threshold`

import co.touchlab.skie.configuration.DefaultArgumentInterop

@DefaultArgumentInterop.MaximumDefaultArgumentCount(1)
fun foo(i: Int = 0, k: Int = 0): Int = i + k
