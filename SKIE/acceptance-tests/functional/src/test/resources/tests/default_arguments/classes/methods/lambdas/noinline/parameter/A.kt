package `tests`.`default_arguments`.`classes`.`methods`.`lambdas`.`noinline`.`parameter`

class A {

    inline fun foo(noinline i: (Int) -> Int = { it + 1 }, noinline k: (Int) -> Int): Int = i(0) - k(1)
}
