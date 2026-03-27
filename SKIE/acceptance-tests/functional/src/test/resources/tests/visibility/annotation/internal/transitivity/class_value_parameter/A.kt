package `tests`.`visibility`.`annotation`.`internal`.`transitivity`.`class_value_parameter`

import co.touchlab.skie.configuration.annotations.SkieVisibility

@SkieVisibility.Internal
class A

fun foo(a: A) {
}
