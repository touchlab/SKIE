package `tests`.`coroutines`.`suspend`.`global_functions`.`return_type`.`unit`.`can_be_called_from_background_thread`

var result = 1

suspend fun foo() {
    result = 0
}
