package `tests`.`default_arguments`.`interfaces`.`extensions`.`generics`.`combined`

import tests.default_arguments.interfaces.extensions.generics.I
import tests.default_arguments.interfaces.extensions.generics.K

interface A<T> where T : I, T : K

class AImpl<T> : A<T> where T : I, T : K

fun <T, U : T> A<T>.foo(
    defaultForDefault: U,
    defaultForReturnValue: U = defaultForDefault,
    returnValue: T = defaultForReturnValue,
): T where T : I, T : K = returnValue
