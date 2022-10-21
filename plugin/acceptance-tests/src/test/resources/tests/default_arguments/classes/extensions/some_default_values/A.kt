package `tests`.`default_arguments`.`classes`.`extensions`.`some_default_values`

class A {

    val one = 1
}

fun A.foo(i: Int, k: Int = one, m: Int, o: Int = 3): Int = i + k * m - o
