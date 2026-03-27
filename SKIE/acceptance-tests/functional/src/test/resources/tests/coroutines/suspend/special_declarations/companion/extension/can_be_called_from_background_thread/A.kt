package `tests`.`coroutines`.`suspend`.`special_declarations`.`companion`.`extension`.`can_be_called_from_background_thread`

class A {

    companion object {

        const val i: Int = 0
    }
}

suspend fun A.Companion.foo(): Int = i
