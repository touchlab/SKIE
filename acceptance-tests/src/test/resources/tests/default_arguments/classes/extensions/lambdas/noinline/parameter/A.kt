package `tests`.`default_arguments`.`classes`.`extensions`.`lambdas`.`noinline`.`parameter`

class A {

    val addOne: (Int) -> Int = { it + 1 }
}

inline fun A.foo(noinline i: (Int) -> Int = addOne, noinline k: (Int) -> Int): Int = i(0) - k(1)
