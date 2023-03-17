package tests.bugs.param_name_escaped_when_force_cast

class A<T> {
    suspend fun foo(init: T): T = init
}
