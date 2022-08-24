package `tests`.`sealed`.`combined`.`class_and_interface`

sealed class A(val value_A: Int)

sealed interface I {

    val value_I: Int
}

class A1(val value_A1: Int, value_A: Int, override val value_I: Int) : A(value_A), I
class A2(val value_A2: Int, value_A: Int) : A(value_A)
class A3(val value_A2: Int, value_A: Int, override val value_I: Int) : A(value_A), I