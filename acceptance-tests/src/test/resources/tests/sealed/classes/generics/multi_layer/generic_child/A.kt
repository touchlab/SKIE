package tests.sealed.classes.generics.multi_layer.generic_child

sealed class A

sealed class A1<T> : A()

class A1A<T : Any>(val value: T) : A1<T>()

class A1B<T> : A1<T>()

class A2 : A()

