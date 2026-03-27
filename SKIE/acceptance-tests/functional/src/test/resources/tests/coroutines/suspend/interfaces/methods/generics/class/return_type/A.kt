package `tests`.`coroutines`.`suspend`.`interfaces`.`methods`.`generics`.`class`.`return_type`

interface A<T : Int> {

    suspend fun foo(i: Int): T = i as T
}

class A1<T : Int> : A<T>
