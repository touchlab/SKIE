package `tests`.`visibility`.`annotation`.`internal`.`interface_extension`

import co.touchlab.skie.configuration.annotations.SkieVisibility

interface I

class A : I

@SkieVisibility.Internal
fun I.foo() {
}
