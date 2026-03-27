package `tests`.`functions`.`file_scope_conversion`.`global_functions`.`functions`.`visibility`.`internal`.`skie`

import co.touchlab.skie.configuration.annotations.SkieVisibility

@SkieVisibility.Internal
fun foo(i: Int, k: Int): Int = i - k
