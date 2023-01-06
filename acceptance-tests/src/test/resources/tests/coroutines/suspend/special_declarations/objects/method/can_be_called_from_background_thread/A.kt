package `tests`.`coroutines`.`suspend`.`special_declarations`.`objects`.`method`.`can_be_called_from_background_thread`

object A {

    const val i: Int = 0

    suspend fun foo(): Int = i
}
