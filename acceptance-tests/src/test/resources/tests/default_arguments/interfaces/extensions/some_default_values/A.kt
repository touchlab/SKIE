package `tests`.`default_arguments`.`interfaces`.`extensions`.`some_default_values`

interface A {

    val one: Int
}

class AImpl : A {

    override val one: Int = 1
}

fun A.foo(i: Int, k: Int = one, m: Int, o: Int = 3): Int = i + k * m - o
