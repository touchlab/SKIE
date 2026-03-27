package `tests`.`default_arguments`.`classes`.`extensions`.`suspend`

class A {

    val zero = 0
}

suspend fun A.foo(i: Int = zero): Int = i
