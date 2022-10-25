package `tests`.`default_arguments`.`interfaces`.`extensions`.`lambdas`.`crossinline`

interface A {

    val one: Int
}

class AImpl : A {

    override val one: Int = 1
}

inline fun A.foo(crossinline i: (Int) -> Int = { it + one }, crossinline k: (Int) -> Int): Int = i(0) - k(1)
