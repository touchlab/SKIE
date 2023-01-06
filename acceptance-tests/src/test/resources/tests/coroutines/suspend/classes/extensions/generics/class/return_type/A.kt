package `tests`.`coroutines`.`suspend`.`classes`.`extensions`.`generics`.`class`.`return_type`

class A<T : Int>

suspend fun <T : Int> A<T>.foo(i: Int): T = i as T
