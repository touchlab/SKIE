package `tests`.`visibility`.`annotation`.`internal`.`transitivity`.`class_return_type`

import co.touchlab.skie.configuration.annotations.SkieVisibility

@SkieVisibility.Internal
class A {
}

fun foo(): A {
    return A()
}
