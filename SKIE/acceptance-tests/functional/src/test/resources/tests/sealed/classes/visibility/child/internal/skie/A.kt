package `tests`.`sealed`.`classes`.`visibility`.`child`.`internal`.`skie`

import co.touchlab.skie.configuration.annotations.SkieVisibility

sealed class A {

    @SkieVisibility.Internal
    class A1 : A()

    class A2(val k: Int) : A()

    companion object {

        fun createA1(): A = A1()
    }
}
