package `tests`.`default_arguments`.`classes`.`extensions`.`lambdas`.`noinline`.`function`

class A {

    val addOne: (Int) -> Int = { it + 1 }
}

fun A.foo(i: (Int) -> Int = addOne, k: (Int) -> Int): Int = i(0) - k(1)
