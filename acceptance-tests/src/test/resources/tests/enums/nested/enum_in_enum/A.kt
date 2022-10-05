package tests.enums.basic.nested

enum class A {
    A1,
    A2;

    enum class B {
        B1,
        B2
    }
}

fun a1(): A = A.A1
fun a2(): A = A.A2
fun b1(): A.B = A.B.B1
fun b2(): A.B = A.B.B2
