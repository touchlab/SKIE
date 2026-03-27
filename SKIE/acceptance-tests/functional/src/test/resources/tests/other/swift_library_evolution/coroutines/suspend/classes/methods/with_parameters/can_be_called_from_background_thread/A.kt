package `tests`.`other`.`swift_library_evolution`.`coroutines`.`suspend`.`classes`.`methods`.`with_parameters`.`can_be_called_from_background_thread`

class A {

    suspend fun foo(i: Int, k: Int): Int = i - k + 1
}
