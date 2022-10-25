package `tests`.`default_arguments`.`classes`.`extensions`.`lambdas`.`inline`

class A {

    val one = 1
}

inline fun A.foo(i: (Int) -> Int = { it + one }, k: (Int) -> Int): Int = i(0) - k(1)
