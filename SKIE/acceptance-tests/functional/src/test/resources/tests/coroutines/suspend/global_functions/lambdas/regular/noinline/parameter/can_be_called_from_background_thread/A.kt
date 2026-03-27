package `tests`.`coroutines`.`suspend`.`global_functions`.`lambdas`.`regular`.`noinline`.`parameter`.`can_be_called_from_background_thread`

suspend inline fun foo(noinline i: () -> Int): Int = i()
