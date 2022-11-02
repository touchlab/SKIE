package `tests`.`enums`.`nested`.`interface_in_enum`

enum class A {
    A1,
    A2;

    interface B {
    }
}

private class C: A.B

fun a1(): A = A.A1
fun a2(): A = A.A2

fun b(): A.B = C()
