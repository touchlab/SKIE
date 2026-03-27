package `tests`.`visibility`.`annotation`.`private`.`transitivity`.`class_extension`

import co.touchlab.skie.configuration.annotations.SkieVisibility

@SkieVisibility.Private
class A

@SkieVisibility.Internal
fun A.foo() {
}
