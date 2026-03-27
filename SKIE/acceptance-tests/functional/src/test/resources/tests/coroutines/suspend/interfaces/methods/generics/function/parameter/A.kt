package `tests`.`coroutines`.`suspend`.`interfaces`.`methods`.`generics`.`function`.`parameter`

interface A {

    suspend fun <T, U : Int> foo(i: T, k: U): Int = k
}

class A1 : A
