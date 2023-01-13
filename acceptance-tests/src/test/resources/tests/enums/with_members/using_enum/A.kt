package `tests`.`enums`.`with_members`.`using_enum`

enum class A {
    A1,
    A2;

    val b: B = B.B1

    fun b1(a: A): B = B.B1
}

enum class B {
    B1
}

fun a1(): A {
    return A.A1
}

fun a2(): A {
    return A.A2
}
