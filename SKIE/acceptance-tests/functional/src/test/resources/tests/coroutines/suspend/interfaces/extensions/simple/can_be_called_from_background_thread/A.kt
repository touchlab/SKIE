package `tests`.`coroutines`.`suspend`.`interfaces`.`extensions`.`simple`.`can_be_called_from_background_thread`

interface A

class A1 : A

suspend fun A.foo(): Int = 0
