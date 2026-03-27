package `tests`.`visibility`.`annotation`.`internal`.`transitivity`.`class_class`

import co.touchlab.skie.configuration.annotations.SkieVisibility

@SkieVisibility.Internal
class A {

    class B {
    }
}
