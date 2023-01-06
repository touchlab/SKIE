package `tests`.`coroutines`.`suspend`.`interfaces`.`methods`.`extension`.`can_be_called_from_background_thread`

interface A {

    val i: Int

    suspend fun B.foo(): Int = i - k
}

class A1(override val i: Int) : A

class B(val k: Int)
