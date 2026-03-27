package `tests`.`default_arguments`.`interfaces`.`extensions`.`generics`.`functions`

import tests.default_arguments.interfaces.extensions.generics.I
import tests.default_arguments.interfaces.extensions.generics.K

interface A

class AImpl : A

fun <T> A.foo(
    defaultForDefault: T,
    defaultForReturnValue: T = defaultForDefault,
    returnValue: T = defaultForReturnValue,
): T where T : I, T : K = returnValue
