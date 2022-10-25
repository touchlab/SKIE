package `tests`.`default_arguments`.`classes`.`methods`.`configuration`.`disabled`.`annotations`

import co.touchlab.skie.configuration.DefaultArgumentInterop

class A {

    @DefaultArgumentInterop.Disabled
    fun foo(i: Int = 0): Int = i
}
