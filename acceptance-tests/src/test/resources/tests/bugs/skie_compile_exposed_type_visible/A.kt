package tests.bugs.skie_compile_exposed_type_visible

class A<T: Any> {
}

suspend fun A<Unit>.execute() {

}
