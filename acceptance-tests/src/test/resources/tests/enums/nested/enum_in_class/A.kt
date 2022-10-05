package tests.enums.nested.enum_in_class

class A {
    enum class B {
        B1,
        B2
    }
}

fun b1(): A.B = A.B.B1
fun b2(): A.B = A.B.B2
