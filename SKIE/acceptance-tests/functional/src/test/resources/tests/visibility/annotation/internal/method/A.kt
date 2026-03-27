package `tests`.`visibility`.`annotation`.`internal`.`method`

import co.touchlab.skie.configuration.annotations.SkieVisibility

class A {

    @SkieVisibility.Internal
    fun foo() {
    }
}
