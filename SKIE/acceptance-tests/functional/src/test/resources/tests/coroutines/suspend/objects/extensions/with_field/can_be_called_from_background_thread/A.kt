package `tests`.`coroutines`.`suspend`.`objects`.`extensions`.`with_field`.`can_be_called_from_background_thread`

object A {

    val i: Int = 0
}

suspend fun A.foo(): Int = i
