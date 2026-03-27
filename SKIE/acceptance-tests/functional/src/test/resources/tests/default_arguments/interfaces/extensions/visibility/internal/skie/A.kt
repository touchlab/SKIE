package `tests`.`default_arguments`.`interfaces`.`extensions`.`visibility`.`internal`.`skie`

import co.touchlab.skie.configuration.annotations.SkieVisibility

interface A

@SkieVisibility.Internal
fun A.foo(i: Int = 0): Int = i
