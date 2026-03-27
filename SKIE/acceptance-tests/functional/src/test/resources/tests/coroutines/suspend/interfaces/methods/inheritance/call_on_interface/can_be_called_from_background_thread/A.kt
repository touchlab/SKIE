package `tests`.`coroutines`.`suspend`.`interfaces`.`methods`.`inheritance`.`call_on_interface`.`can_be_called_from_background_thread`

interface A {

    suspend fun foo(): Int = 1
}

class A1 : A {

    override suspend fun foo(): Int = 0
}
