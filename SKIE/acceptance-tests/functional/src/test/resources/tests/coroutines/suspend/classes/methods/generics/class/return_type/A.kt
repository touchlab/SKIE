package `tests`.`coroutines`.`suspend`.`classes`.`methods`.`generics`.`class`.`return_type`

class A<T : Int> {

    suspend fun foo(i: Int): T = i as T
}
