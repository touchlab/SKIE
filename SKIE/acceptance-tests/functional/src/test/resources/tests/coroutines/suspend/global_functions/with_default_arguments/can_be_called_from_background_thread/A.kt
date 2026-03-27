package `tests`.`coroutines`.`suspend`.`global_functions`.`with_default_arguments`.`can_be_called_from_background_thread`

suspend fun foo(i: Int = 0, k: Int = 1): Int = i - k + 1
