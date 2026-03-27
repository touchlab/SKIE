package `tests`.`coroutines`.`suspend`.`objects`.`methods`.`inheritance`.`open`.`can_be_called_from_background_thread`

open class A {

    open suspend fun foo(): Int = 1
}

object A1 : A() {

    override suspend fun foo(): Int = 0
}
