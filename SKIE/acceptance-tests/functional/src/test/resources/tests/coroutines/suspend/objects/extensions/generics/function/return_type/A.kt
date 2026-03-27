package `tests`.`coroutines`.`suspend`.`objects`.`extensions`.`generics`.`function`.`return_type`

object A

suspend fun <T : Int> A.foo(i: Int): T = i as T
