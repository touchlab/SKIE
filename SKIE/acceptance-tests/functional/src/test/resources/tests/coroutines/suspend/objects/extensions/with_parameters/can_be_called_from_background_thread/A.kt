package `tests`.`coroutines`.`suspend`.`objects`.`extensions`.`with_parameters`.`can_be_called_from_background_thread`

object A

suspend fun A.foo(i: Int, k: Int): Int = i - k + 1
