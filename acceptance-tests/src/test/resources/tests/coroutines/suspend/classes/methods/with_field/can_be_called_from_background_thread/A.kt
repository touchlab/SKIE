package `tests`.`coroutines`.`suspend`.`classes`.`methods`.`with_field`.`can_be_called_from_background_thread`

class A(val i: Int) {

    suspend fun foo(): Int = i
}
