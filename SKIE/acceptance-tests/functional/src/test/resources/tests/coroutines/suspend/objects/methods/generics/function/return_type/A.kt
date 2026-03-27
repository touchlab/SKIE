package `tests`.`coroutines`.`suspend`.`objects`.`methods`.`generics`.`function`.`return_type`

object A {

    suspend fun <T : Int> foo(i: Int): T = i as T
}
