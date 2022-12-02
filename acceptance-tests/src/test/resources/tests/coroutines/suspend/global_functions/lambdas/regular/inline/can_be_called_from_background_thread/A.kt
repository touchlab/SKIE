package `tests`.`coroutines`.`suspend`.`global_functions`.`lambdas`.`regular`.`inline`.`can_be_called_from_background_thread`

suspend inline fun foo(i: () -> Int): Int = i()
