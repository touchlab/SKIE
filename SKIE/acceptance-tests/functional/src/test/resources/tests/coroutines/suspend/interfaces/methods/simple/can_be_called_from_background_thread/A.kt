package `tests`.`coroutines`.`suspend`.`interfaces`.`methods`.`simple`.`can_be_called_from_background_thread`

interface A {

    suspend fun foo(): Int = 0
}

class A1 : A
