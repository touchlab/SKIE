package `tests`.`visibility`.`annotation`.`private`.`transitivity`.`interface_extension`

import co.touchlab.skie.configuration.annotations.SkieVisibility

@SkieVisibility.Private
interface I

class A : I

@SkieVisibility.Internal
fun I.foo() {
}
