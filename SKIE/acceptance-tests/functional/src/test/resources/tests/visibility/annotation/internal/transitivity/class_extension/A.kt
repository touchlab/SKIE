package `tests`.`visibility`.`annotation`.`internal`.`transitivity`.`class_extension`

import co.touchlab.skie.configuration.annotations.SkieVisibility

@SkieVisibility.Internal
class A

fun A.foo() {
}
