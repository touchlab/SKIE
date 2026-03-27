package `tests`.`coroutines`.`suspend`.`objects`.`methods`.`with_parameters`.`can_be_called_from_background_thread`

object A {

    suspend fun foo(i: Int, k: Int): Int = i - k + 1
}
