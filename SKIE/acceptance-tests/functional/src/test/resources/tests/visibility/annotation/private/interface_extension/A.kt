package `tests`.`visibility`.`annotation`.`private`.`interface_extension`

import co.touchlab.skie.configuration.annotations.SkieVisibility

interface I

class A : I

@SkieVisibility.Private
fun I.foo() {
}
