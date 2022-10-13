package `tests`.`sealed`.`classes`.`generics`.`passed_from_children_to_parent`

sealed class A<T>(val value: T)

class A1<T>(value: T) : A<T>(value)
