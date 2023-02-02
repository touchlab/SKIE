package `tests`.`coroutines`.`suspend`.`classes`.`extensions`.`inheritance`.`can_be_called_from_background_thread`

open class A

class B: A()

suspend fun A.foo() = 0
