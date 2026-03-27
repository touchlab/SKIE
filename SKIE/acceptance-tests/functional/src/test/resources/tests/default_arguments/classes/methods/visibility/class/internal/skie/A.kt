package `tests`.`default_arguments`.`classes`.`methods`.`visibility`.`class`.`internal`.`skie`

import co.touchlab.skie.configuration.annotations.SkieVisibility

@SkieVisibility.Internal
class A {

    fun foo(i: Int = 0): Int = i
}
