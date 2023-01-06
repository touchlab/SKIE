package `tests`.`coroutines`.`suspend`.`interfaces`.`extensions`.`with_field`.`can_be_called_from_background_thread`

interface A {

    val i: Int
}

class A1(override val i: Int) : A

suspend fun A.foo(): Int = i
