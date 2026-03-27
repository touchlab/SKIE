package `tests`.`coroutines`.`suspend`.`global_functions`.`with_parameter_named_suspendHandler`.`can_be_called_from_background_thread`

suspend fun foo(suspendHandler: Int, _suspendHandler: Int): Int = suspendHandler - _suspendHandler + 1
