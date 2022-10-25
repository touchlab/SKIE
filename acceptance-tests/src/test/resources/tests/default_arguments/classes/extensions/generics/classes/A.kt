package `tests`.`default_arguments`.`classes`.`extensions`.`generics`.`classes`

import tests.default_arguments.classes.extensions.generics.I
import tests.default_arguments.classes.extensions.generics.K

class A<T>(val defaultForDefault: T) where T : I, T : K

fun <T> A<T>.foo(defaultForReturnValue: T = defaultForDefault, returnValue: T = defaultForReturnValue): T where T : I, T : K = returnValue
