package `tests`.`coroutines`.`suspend`.`special_declarations`.`objects`.`extension`.`can_be_called_from_background_thread`

object A {

    const val i: Int = 0
}

suspend fun A.foo(): Int = i
