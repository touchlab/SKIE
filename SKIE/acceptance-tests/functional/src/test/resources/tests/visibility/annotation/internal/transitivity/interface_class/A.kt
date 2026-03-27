package `tests`.`visibility`.`annotation`.`internal`.`transitivity`.`interface_class`

import co.touchlab.skie.configuration.annotations.SkieVisibility

@SkieVisibility.Internal
interface A {

    class B {
    }
}
