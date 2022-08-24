package `tests`.`sealed`.`interfaces`.`with_field`

sealed interface A {

    val value_A: Int
}

class A1(val value_A1: Int, override val value_A: Int) : A
class A2(val value_A2: Int, override val value_A: Int) : A