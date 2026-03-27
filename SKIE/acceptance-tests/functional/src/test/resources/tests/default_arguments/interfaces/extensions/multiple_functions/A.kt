package `tests`.`default_arguments`.`interfaces`.`extensions`.`multiple_functions`

interface A {

    val zero: Int
    val one: Int
}

class AImpl : A {

    override val zero: Int = 0
    override val one: Int = 1
}

fun A.bar(i: Int, k: Int = one): Int = i + k

fun A.foo(i: Int = zero, k: Int): Int = i + k
