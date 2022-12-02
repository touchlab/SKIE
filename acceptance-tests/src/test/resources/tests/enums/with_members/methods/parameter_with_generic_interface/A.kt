package `tests`.`enums`.`with_members`.`methods`.`parameter_with_generic_interface`

enum class A {
    A1;

    fun <T> foo(i: I<T>): Int = i.zero
}

interface I<T> {

    val zero: Int
}

class X : I<Int> {

    override val zero: Int = 0
}

fun a1(): A = A.A1
