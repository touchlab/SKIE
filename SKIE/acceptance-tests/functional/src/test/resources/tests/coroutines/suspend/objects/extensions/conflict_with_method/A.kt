package `tests`.`coroutines`.`suspend`.`objects`.`extensions`.`conflict_with_method`

object A {

    suspend fun foo(): Int = 0
}

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
suspend fun A.foo(): Int = 1
