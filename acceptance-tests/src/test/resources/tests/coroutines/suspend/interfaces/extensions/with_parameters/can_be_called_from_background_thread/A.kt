package `tests`.`coroutines`.`suspend`.`interfaces`.`extensions`.`with_parameters`.`can_be_called_from_background_thread`

interface A

class A1 : A

suspend fun A.foo(i: Int, k: Int): Int = i - k + 1
