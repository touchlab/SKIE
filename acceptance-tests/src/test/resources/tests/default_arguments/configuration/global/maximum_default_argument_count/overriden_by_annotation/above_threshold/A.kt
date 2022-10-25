package `tests`.`default_arguments`.`configuration`.`global`.`maximum_default_argument_count`.`overriden_by_annotation`.`above_threshold`

import co.touchlab.skie.configuration.DefaultArgumentInterop

@DefaultArgumentInterop.MaximumDefaultArgumentCount(2)
fun foo(i: Int = 0, k: Int = 0, m: Int = 0): Int = i + k + m
