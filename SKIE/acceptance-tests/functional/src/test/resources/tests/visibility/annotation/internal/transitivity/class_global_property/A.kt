package `tests`.`visibility`.`annotation`.`internal`.`transitivity`.`class_global_property`

import co.touchlab.skie.configuration.annotations.SkieVisibility

@SkieVisibility.Internal
class A {
}

val foo: A = A()
