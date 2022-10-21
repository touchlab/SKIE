package `tests`.`default_arguments`.`classes`.`extensions`.`generics`.`functions`

import tests.default_arguments.classes.extensions.generics.I
import tests.default_arguments.classes.extensions.generics.K

class A

fun <T> A.foo(
    defaultForDefault: T,
    defaultForReturnValue: T = defaultForDefault,
    returnValue: T = defaultForReturnValue,
): T where T : I, T : K = returnValue
