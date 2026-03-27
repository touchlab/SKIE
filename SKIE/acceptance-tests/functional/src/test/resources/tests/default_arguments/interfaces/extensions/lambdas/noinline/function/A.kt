package `tests`.`default_arguments`.`interfaces`.`extensions`.`lambdas`.`noinline`.`function`

interface A {

    val addOne: (Int) -> Int
}

class AImpl : A {

    override val addOne: (Int) -> Int = { it + 1 }
}

fun A.foo(i: (Int) -> Int = addOne, k: (Int) -> Int): Int = i(0) - k(1)
