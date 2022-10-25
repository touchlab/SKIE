package `tests`.`default_arguments`.`interfaces`.`extensions`.`generics`.`classes`

import tests.default_arguments.interfaces.extensions.generics.I
import tests.default_arguments.interfaces.extensions.generics.K

interface A<T> where T : I, T : K {
    val defaultForDefault: T
}

class AImpl<T>(override val defaultForDefault: T) : A<T> where T : I, T : K

fun <T> A<T>.foo(defaultForReturnValue: T = defaultForDefault, returnValue: T = defaultForReturnValue): T where T : I, T : K = returnValue
