package `tests`.`sealed`.`interfaces`.`generics`.`passed_from_children_to_parent`

sealed interface A<T> {

    val value: T
}

class A1<T>(override val value: T) : A<T>
