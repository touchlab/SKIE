package `tests`.`default_arguments`.`classes`.`methods`.`visibility`.`function`.`internal`.`skie`

import co.touchlab.skie.configuration.annotations.SkieVisibility

class A {

    @SkieVisibility.Internal
    fun foo(i: Int = 0): Int = i
}
