package tests.default_arguments.classes.methods.generics.classes

import tests.default_arguments.classes.methods.generics.I
import tests.default_arguments.classes.methods.generics.K

class A<T>(private val defaultForDefault: T) where T : I, T : K {

    fun foo(defaultForReturnValue: T = defaultForDefault, returnValue: T = defaultForReturnValue): T = returnValue
}
