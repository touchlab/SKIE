package `tests`.`visibility`.`annotation`.`internal`.`global_function`

import co.touchlab.skie.configuration.annotations.SkieVisibility

interface I

class A : I

@SkieVisibility.InternalIfWrapped
var I.foo: Int
    get() = 0
    set(value) {
    }
