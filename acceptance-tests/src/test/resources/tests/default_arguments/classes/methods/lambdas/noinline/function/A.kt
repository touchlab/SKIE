package `tests`.`default_arguments`.`classes`.`methods`.`lambdas`.`noinline`.`function`

class A {

    fun foo(i: (Int) -> Int = { it + 1 }, k: (Int) -> Int): Int = i(0) - k(1)
}