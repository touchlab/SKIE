package `tests`.`coroutines`.`suspend`.`objects`.`extensions`.`generics`.`function`.`parameter`

object A

suspend fun <T, U : Int> A.foo(i: T, k: U): Int = k
