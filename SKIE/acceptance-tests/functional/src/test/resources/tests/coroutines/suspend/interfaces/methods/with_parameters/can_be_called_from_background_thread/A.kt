package `tests`.`coroutines`.`suspend`.`interfaces`.`methods`.`with_parameters`.`can_be_called_from_background_thread`

interface A {

    suspend fun foo(i: Int, k: Int): Int = i - k + 1
}

class A1 : A
