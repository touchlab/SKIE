package `tests`.`sealed`.`interfaces`.`generics`.`new_in_children`.`interface`

sealed interface A

interface A1<T> : A {

    val value: T
}

class A1A<T>(override val value: T) : A1<T>
