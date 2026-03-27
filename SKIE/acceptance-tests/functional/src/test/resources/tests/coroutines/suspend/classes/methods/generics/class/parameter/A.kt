package `tests`.`coroutines`.`suspend`.`classes`.`methods`.`generics`.`class`.`parameter`

class A<T, U : Int> {

    suspend fun foo(i: T, k: U): Int = k
}
