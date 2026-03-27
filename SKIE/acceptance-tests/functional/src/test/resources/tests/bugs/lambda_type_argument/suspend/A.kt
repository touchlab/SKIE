package `tests`.`bugs`.`lambda_type_argument`.`suspend`

class A<T>

suspend fun parameter(a: A<() -> Unit>) {
}

suspend fun returnType(): A<() -> Unit> {
    error("")
}

suspend fun <T : A<() -> Unit>> typeConstraint(a: T) {
}
