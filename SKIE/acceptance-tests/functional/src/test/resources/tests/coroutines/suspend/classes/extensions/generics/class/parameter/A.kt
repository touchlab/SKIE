package `tests`.`coroutines`.`suspend`.`classes`.`extensions`.`generics`.`class`.`parameter`

class A<T, U : Int>

suspend fun <T, U : Int> A<T, U>.foo(i: T, k: U): Int = k
