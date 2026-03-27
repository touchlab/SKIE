package `tests`.`coroutines`.`suspend`.`special_declarations`.`companion`.`method`.`can_be_called_from_background_thread`

class A {

    companion object {

        const val i: Int = 0

        suspend fun foo(): Int = i
    }
}
