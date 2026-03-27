package `tests`.`coroutines`.`suspend`.`global_functions`.`lambdas`.`regular`.`crossinline`.`can_be_called_from_background_thread`

suspend inline fun foo(crossinline i: () -> Int): Int = i()
