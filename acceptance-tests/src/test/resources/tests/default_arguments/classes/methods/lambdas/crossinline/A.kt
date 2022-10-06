package `tests`.`default_arguments`.`classes`.`methods`.`lambdas`.`crossinline`

class A {

    inline fun foo(crossinline i: (Int) -> Int = { it + 1 }, crossinline k: (Int) -> Int): Int = i(0) - k(1)
}
