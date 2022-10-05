package tests.enums.with_members.generic

enum class A {
    A1,
    A2;

    val <T> T.a: T get() = this

    fun <T> foo(t: T) = t
}

fun a1(): A = A.A1

fun a2(): A = A.A2
