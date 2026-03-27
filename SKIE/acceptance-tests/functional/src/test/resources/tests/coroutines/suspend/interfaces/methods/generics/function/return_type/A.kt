package `tests`.`coroutines`.`suspend`.`interfaces`.`methods`.`generics`.`function`.`return_type`

interface A {

    suspend fun <T : Int> foo(i: Int): T = i as T
}

class A1 : A
