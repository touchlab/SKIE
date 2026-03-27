package `tests`.`coroutines`.`suspend`.`special_declarations`.`suspend`.`with_collision`

class A {

    suspend fun foo(i: Int = 0, k: Int = 1): Int = i - k

    suspend fun foo(i: Int): Int = i
}
