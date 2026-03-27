package `tests`.`default_arguments`.`global_functions`.`visibility`.`internal`.`skie`

import co.touchlab.skie.configuration.annotations.SkieVisibility

@SkieVisibility.PublicButReplaced
fun foo(i: Int = 0): Int = i
