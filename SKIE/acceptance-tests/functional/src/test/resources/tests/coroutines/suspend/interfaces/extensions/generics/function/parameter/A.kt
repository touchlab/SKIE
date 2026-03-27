package `tests`.`coroutines`.`suspend`.`interfaces`.`extensions`.`generics`.`function`.`parameter`

interface A

class A1 : A

suspend fun <T, U : Int> A.foo(i: T, k: U): Int = k
