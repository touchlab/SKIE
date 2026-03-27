package `tests`.`default_arguments`.`classes`.`methods`.`generics`.`functions`

import tests.default_arguments.classes.methods.generics.I
import tests.default_arguments.classes.methods.generics.K

class A {

    fun <T> foo(
        defaultForDefault: T,
        defaultForReturnValue: T = defaultForDefault,
        returnValue: T = defaultForReturnValue,
    ): T where T : I, T : K = returnValue
}
