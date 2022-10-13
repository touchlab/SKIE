package tests.default_arguments.classes.methods.generics.combined

import tests.default_arguments.classes.methods.generics.I
import tests.default_arguments.classes.methods.generics.K

class A<T> where T : I, T : K {

    fun <U : T> foo(
        defaultForDefault: U,
        defaultForReturnValue: U = defaultForDefault,
        returnValue: T = defaultForReturnValue,
    ): T = returnValue
}
