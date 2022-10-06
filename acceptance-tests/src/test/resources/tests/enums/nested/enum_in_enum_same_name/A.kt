package `tests`.`enums`.`nested`.`enum_in_enum_same_name`

enum class A {
    A1,
    A2;

    enum class A {
        A1,
        A2,
    }
}

fun a1(): A = A.A1
fun a2(): A = A.A2
fun aa1(): A.A = A.A.A1
fun aa2(): A.A = A.A.A2
