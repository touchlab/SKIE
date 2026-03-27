package `tests`.`default_arguments`.`interfaces`.`extensions`.`lambdas`.`noinline`.`parameter`

interface A {

    val addOne: (Int) -> Int
}

class AImpl : A {

    override val addOne: (Int) -> Int = { it + 1 }
}

inline fun A.foo(noinline i: (Int) -> Int = addOne, noinline k: (Int) -> Int): Int = i(0) - k(1)
