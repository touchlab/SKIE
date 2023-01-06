package `tests`.`coroutines`.`suspend`.`interfaces`.`extensions`.`generics`.`class`.`return_type`

interface A<T : Int>

class A1<T : Int> : A<T>

suspend fun <T : Int> A<T>.foo(i: Int): T = i as T
