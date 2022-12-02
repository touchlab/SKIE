package `tests`.`coroutines`.`suspend`.`global_functions`.`lambdas`.`regular`.`noinline`.`function`.`can_be_called_from_background_thread`

suspend fun foo(i: () -> Int): Int = i()
