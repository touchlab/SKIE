package `tests`.`visibility`.`annotation`.`private`.`transitivity`.`class_value_parameter`

import co.touchlab.skie.configuration.annotations.SkieVisibility

@SkieVisibility.Private
class A

@SkieVisibility.Internal
fun foo(a: A) {
}
