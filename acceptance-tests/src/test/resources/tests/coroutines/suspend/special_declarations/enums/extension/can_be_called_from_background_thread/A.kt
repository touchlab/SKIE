package `tests`.`coroutines`.`suspend`.`special_declarations`.`enums`.`extension`.`can_be_called_from_background_thread`

enum class A(val i: Int) {
    A1(0)
}

suspend fun A.foo(): Int = i
