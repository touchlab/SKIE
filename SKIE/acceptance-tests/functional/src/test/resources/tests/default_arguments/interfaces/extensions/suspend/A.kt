package `tests`.`default_arguments`.`interfaces`.`extensions`.`suspend`

interface A {

    val zero: Int
}

class AImpl : A {

    override val zero: Int = 0
}

suspend fun A.foo(i: Int = zero): Int = i
