package `tests`.`coroutines`.`suspend`.`global_functions`.`generics`.`return_type`

suspend fun <T : Int> foo(i: Int): T = i as T
