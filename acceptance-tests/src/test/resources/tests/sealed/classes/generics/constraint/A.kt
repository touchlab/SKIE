package `tests`.`sealed`.`classes`.`generics`.`constraint`

sealed class A<T : Wrapper<*>>(val wrapper: T)

class A1<T : Wrapper<*>>(wrapper: T) : A<T>(wrapper)

interface Wrapper<T> {

    val value: T
}

class IntWrapper(override val value: Int) : Wrapper<Int>