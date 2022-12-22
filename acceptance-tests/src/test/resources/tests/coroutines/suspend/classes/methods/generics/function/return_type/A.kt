package `tests`.`coroutines`.`suspend`.`classes`.`methods`.`generics`.`function`.`return_type`

class A {

    suspend fun <T : Int> foo(i: Int): T = i as T
}
