package `tests`.`enums`.`nested`.`value_class_in_enum`

enum class A {
    A1,
    A2;

    @JvmInline
    value class B(val value: Int)
}

fun a1(): A = A.A1
fun a2(): A = A.A2

fun b(value: Int): A.B = A.B(value)
