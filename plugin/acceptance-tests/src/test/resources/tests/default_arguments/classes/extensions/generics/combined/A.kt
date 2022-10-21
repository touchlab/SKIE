package `tests`.`default_arguments`.`classes`.`extensions`.`generics`.`combined`

import tests.default_arguments.classes.extensions.generics.I
import tests.default_arguments.classes.extensions.generics.K

class A<T> where T : I, T : K

fun <T, U : T> A<T>.foo(
    defaultForDefault: U,
    defaultForReturnValue: U = defaultForDefault,
    returnValue: T = defaultForReturnValue,
): T where T : I, T : K = returnValue
