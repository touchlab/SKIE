package `tests`.`coroutines`.`suspend`.`special_declarations`.`data_class`.`method`.`can_be_called_from_background_thread`

data class A(val i: Int) {

    suspend fun foo(): Int = i
}
