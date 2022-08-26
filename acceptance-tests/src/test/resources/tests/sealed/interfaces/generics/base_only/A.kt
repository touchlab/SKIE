package `tests`.`sealed`.`interfaces`.`generics`.`base_only`

sealed interface A<T> {

    val value: T
}

class A1(override val value: Int) : A<Int>