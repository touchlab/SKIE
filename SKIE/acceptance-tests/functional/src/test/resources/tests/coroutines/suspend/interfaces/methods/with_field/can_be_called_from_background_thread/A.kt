package `tests`.`coroutines`.`suspend`.`interfaces`.`methods`.`with_field`.`can_be_called_from_background_thread`

interface A {

    val i: Int

    suspend fun foo(): Int = i
}

class A1(override val i: Int) : A
