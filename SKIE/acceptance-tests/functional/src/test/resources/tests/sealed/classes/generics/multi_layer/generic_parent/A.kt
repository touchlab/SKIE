package `tests`.`sealed`.`classes`.`generics`.`multi_layer`.`generic_parent`

sealed class A<T : Any>(val value: T)

sealed class A1(value: Int) : A<Int>(value)

class A1A(value: Int) : A1(value)

class A1B(value: Int) : A1(value)

class A2 : A<Int>(0)
