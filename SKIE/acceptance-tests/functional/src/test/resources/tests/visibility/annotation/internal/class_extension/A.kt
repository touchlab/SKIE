package `tests`.`visibility`.`annotation`.`internal`.`class_extension`

import co.touchlab.skie.configuration.annotations.SkieVisibility

class A

@SkieVisibility.Internal
fun A.foo() {
}
