package `tests`.`coroutines`.`suspend`.`objects`.`methods`.`inheritance`.`abstract`.`can_be_called_from_background_thread`

abstract class A {

    abstract suspend fun foo(): Int
}

object A1 : A() {

    override suspend fun foo(): Int = 0
}
