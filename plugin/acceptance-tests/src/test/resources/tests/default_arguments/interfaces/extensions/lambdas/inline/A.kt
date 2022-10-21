package `tests`.`default_arguments`.`interfaces`.`extensions`.`lambdas`.`inline`

interface A {

    val one: Int
}

class AImpl : A {

    override val one: Int = 1
}

inline fun A.foo(i: (Int) -> Int = { it + one }, k: (Int) -> Int): Int = i(0) - k(1)
