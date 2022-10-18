package `tests`.`enums`.`nested`.`class_in_enum`

enum class A {
    A1,
    A2;

    class B {

    }
}

fun a1(): A = A.A1
fun a2(): A = A.A2

fun b(): A.B = A.B()
