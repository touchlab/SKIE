package `tests`.`sealed`.`classes`.`generics`.`base_only`

sealed class A<T>(val value: T)

class A1(value: Int) : A<Int>(value)
