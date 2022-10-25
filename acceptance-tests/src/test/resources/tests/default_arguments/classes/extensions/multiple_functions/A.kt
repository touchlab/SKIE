package `tests`.`default_arguments`.`classes`.`extensions`.`multiple_functions`

class A {

    val zero = 0
    val one = 1
}

fun A.bar(i: Int, k: Int = one): Int = i + k

fun A.foo(i: Int = zero, k: Int): Int = i + k
