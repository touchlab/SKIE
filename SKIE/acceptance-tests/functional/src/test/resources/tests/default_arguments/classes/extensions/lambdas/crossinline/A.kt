package `tests`.`default_arguments`.`classes`.`extensions`.`lambdas`.`crossinline`

class A {

    val one = 1
}

inline fun A.foo(crossinline i: (Int) -> Int = { it + one }, crossinline k: (Int) -> Int): Int = i(0) - k(1)
