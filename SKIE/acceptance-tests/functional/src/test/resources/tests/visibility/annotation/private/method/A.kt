package `tests`.`visibility`.`annotation`.`private`.`method`

import co.touchlab.skie.configuration.annotations.SkieVisibility

class A {

    @SkieVisibility.Private
    fun foo() {
    }
}
