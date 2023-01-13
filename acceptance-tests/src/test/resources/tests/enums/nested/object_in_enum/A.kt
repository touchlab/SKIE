package tests.enums.nested.object_in_enum

enum class A {
    A1,
    A2;

    object B {

    }
}

fun a1(): A = A.A1
fun a2(): A = A.A2

fun b(): A.B = A.B
