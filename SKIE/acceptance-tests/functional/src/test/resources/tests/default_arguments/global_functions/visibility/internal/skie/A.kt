package `tests`.`default_arguments`.`global_functions`.`visibility`.`internal`.`skie`

import co.touchlab.skie.configuration.annotations.SkieVisibility

@SkieVisibility.Internal
fun foo(i: Int = 0): Int = i
