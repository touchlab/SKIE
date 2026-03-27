package `tests`.`coroutines`.`suspend`.`global_functions`.`vararg`.`can_be_called_from_background_thread`

suspend fun foo(vararg i: Int): Int = i.sum()
