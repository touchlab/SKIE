package `tests`.`default_arguments`.`classes`.`methods`.`lambdas`.`inline`

class A {

    inline fun foo(i: (Int) -> Int = { it + 1 }, k: (Int) -> Int): Int = i(0) - k(1)
}
