package `tests`.`coroutines`.`suspend`.`classes`.`extensions`.`generics`.`function`.`parameter`

class A

suspend fun <T, U : Int> A.foo(i: T, k: U): Int = k
