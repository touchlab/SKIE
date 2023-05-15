package tests.enums.conversion.enum_with_interface

interface I {

    val value: Int
        get() = 0
}

enum class A(override val value: Int) : I {
    A0(0),
    A1(1);
}

fun getIValue(i: I): Int {
    return i.value
}
