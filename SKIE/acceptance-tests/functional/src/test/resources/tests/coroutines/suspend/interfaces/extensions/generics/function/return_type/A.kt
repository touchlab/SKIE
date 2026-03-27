package `tests`.`coroutines`.`suspend`.`interfaces`.`extensions`.`generics`.`function`.`return_type`

interface A

class A1 : A

suspend fun <T : Int> A.foo(i: Int): T = i as T
