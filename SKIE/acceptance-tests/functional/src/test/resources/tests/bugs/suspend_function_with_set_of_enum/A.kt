package `tests`.`bugs`.`suspend_function_with_set_of_enum`

enum class A {

    A, B, C
}

suspend fun foo(set: Set<A>) {
}
