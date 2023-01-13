package tests.enums.with_members.methods.with_enum

enum class A {
    A1;

    fun foo(b: B): B = b
}

enum class B {
    B1;
}

fun a1(): A = A.A1

fun b1(): B = B.B1
