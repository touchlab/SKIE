package `tests`.`coroutines`.`suspend`.`objects`.`methods`.`generics`.`function`.`parameter`

object A {

    suspend fun <T, U : Int> foo(i: T, k: U): Int = k
}
