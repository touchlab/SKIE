package `tests`.`visibility`.`annotation`.`internal`.`transitivity`.`interface_extension`

import co.touchlab.skie.configuration.annotations.SkieVisibility

@SkieVisibility.Internal
interface I

class A : I

fun I.foo() {
}
