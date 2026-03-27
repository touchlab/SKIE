package `tests`.`coroutines`.`suspend`.`objects`.`methods`.`with_field`.`can_be_called_from_background_thread`

object A {

    val i: Int = 0

    suspend fun foo(): Int = i
}
