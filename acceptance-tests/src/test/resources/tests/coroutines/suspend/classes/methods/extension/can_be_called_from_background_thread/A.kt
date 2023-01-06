package `tests`.`coroutines`.`suspend`.`classes`.`methods`.`extension`.`can_be_called_from_background_thread`

class A(val i: Int) {

    suspend fun B.foo(): Int = i - k
}

class B(val k: Int)
