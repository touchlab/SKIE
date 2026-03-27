package `tests`.`visibility`.`annotation`.`private`.`transitivity`.`class_return_type`

import co.touchlab.skie.configuration.annotations.SkieVisibility

@SkieVisibility.Private
class A {
}

@SkieVisibility.Internal
fun foo(): A {
    return A()
}
