package `tests`.`coroutines`.`suspend`.`classes`.`methods`.`inheritance`.`open`.`can_be_called_from_background_thread`

open class A {

    open suspend fun foo(): Int = 1
}

class A1 : A() {

    override suspend fun foo(): Int = 0
}
