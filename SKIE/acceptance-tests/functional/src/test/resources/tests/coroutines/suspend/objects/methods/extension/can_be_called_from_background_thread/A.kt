package `tests`.`coroutines`.`suspend`.`objects`.`methods`.`extension`.`can_be_called_from_background_thread`

object A {

    val i: Int = 1

    suspend fun B.foo(): Int = i - k
}

class B(val k: Int)
