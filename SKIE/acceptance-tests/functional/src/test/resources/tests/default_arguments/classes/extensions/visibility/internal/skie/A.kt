package `tests`.`default_arguments`.`classes`.`extensions`.`visibility`.`internal`.`skie`

import co.touchlab.skie.configuration.annotations.SkieVisibility

class A

@SkieVisibility.Internal
fun A.foo(i: Int = 0): Int = i
