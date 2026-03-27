package `tests`.`coroutines`.`suspend`.`interfaces`.`extensions`.`conflict_with_method`

interface A {

    suspend fun foo(): Int = 0
}

class A1 : A

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
suspend fun A.foo(): Int = 1
