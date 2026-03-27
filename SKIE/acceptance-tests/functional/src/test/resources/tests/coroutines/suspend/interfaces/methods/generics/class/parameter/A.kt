package `tests`.`coroutines`.`suspend`.`interfaces`.`methods`.`generics`.`class`.`parameter`

interface A<T, U : Int> {

    suspend fun foo(i: T, k: U): Int = k
}

class A1<T, U : Int> : A<T, U>
