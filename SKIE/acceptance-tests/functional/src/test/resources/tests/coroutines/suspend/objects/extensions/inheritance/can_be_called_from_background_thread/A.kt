package `tests`.`coroutines`.`suspend`.`objects`.`extensions`.`inheritance`.`can_be_called_from_background_thread`

open class A

object B : A()

suspend fun A.foo() = 0
