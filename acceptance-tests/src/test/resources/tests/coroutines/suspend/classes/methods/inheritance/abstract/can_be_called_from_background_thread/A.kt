package `tests`.`coroutines`.`suspend`.`classes`.`methods`.`inheritance`.`abstract`.`can_be_called_from_background_thread`

abstract class A {

    abstract suspend fun foo(): Int
}

class A1 : A() {

    override suspend fun foo(): Int = 0
}
