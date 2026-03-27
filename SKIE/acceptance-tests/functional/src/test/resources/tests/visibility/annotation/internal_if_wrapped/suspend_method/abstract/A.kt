package `tests`.`visibility`.`annotation`.`internal`.`global_function`

import co.touchlab.skie.configuration.annotations.SkieVisibility

abstract class A {

    @SkieVisibility.InternalIfWrapped
    abstract suspend fun foo()
}

class B : A() {

    override suspend fun foo() {
    }
}
