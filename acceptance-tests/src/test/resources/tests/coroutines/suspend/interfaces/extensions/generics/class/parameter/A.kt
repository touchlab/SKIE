package `tests`.`coroutines`.`suspend`.`interfaces`.`extensions`.`generics`.`class`.`parameter`

interface A<T, U : Int>

class A1<T, U : Int> : A<T, U>

suspend fun <T, U : Int> A<T, U>.foo(i: T, k: U): Int = k
