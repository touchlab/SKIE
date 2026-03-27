package `tests`.`visibility`.`annotation`.`private`.`class_extension`

import co.touchlab.skie.configuration.annotations.SkieVisibility

class A

@SkieVisibility.Private
fun A.foo() {
}
