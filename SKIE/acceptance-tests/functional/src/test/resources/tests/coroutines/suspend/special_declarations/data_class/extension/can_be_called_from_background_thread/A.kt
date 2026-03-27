package `tests`.`coroutines`.`suspend`.`special_declarations`.`data_class`.`extension`.`can_be_called_from_background_thread`

data class A(val i: Int = 0)

suspend fun A.foo(): Int = i
