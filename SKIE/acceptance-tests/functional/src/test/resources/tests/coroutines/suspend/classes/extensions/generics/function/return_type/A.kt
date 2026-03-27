package `tests`.`coroutines`.`suspend`.`classes`.`extensions`.`generics`.`function`.`return_type`

class A

suspend fun <T : Int> A.foo(i: Int): T = i as T
