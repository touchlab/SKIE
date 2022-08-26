package `tests`.`sealed`.`interfaces`.`generics`.`constraint`

sealed interface A<T: Wrapper<*>> {

    val wrapper: T
}

class A1<T: Wrapper<*>>(override val wrapper: T) : A<T>

interface Wrapper<T> {

    val value: T
}

class IntWrapper(override val value: Int) : Wrapper<Int>