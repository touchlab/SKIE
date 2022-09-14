package tests.enums.with_members.methods

enum class A {
    A1,
    A2;

    fun foo() = 0

    fun bar(param: Int) = param

    @Throws(Exception::class)
    fun throwingFoo() { }

    suspend fun suspendingFoo() { }
}

fun randomA(): A {
    return A.values().random()
}
