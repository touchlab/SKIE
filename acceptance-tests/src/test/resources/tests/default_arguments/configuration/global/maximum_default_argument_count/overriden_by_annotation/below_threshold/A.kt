package `tests`.`default_arguments`.`configuration`.`global`.`maximum_default_argument_count`.`overriden_by_annotation`.`below_threshold`

import co.touchlab.skie.configuration.annotations.DefaultArgumentInterop

@DefaultArgumentInterop.MaximumDefaultArgumentCount(2)
fun foo(i: Int = 0, k: Int, m: Int = 0): Int = i + k + m
