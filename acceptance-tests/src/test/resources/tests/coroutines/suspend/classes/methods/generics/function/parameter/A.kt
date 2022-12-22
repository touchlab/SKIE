package `tests`.`coroutines`.`suspend`.`classes`.`methods`.`generics`.`function`.`parameter`

class A {

    suspend fun <T, U : Int> foo(i: T, k: U): Int = k
}
