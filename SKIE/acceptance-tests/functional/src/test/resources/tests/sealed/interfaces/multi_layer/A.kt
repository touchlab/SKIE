package `tests`.`sealed`.`interfaces`.`multi_layer`

sealed interface A {

    val value_A: Int
}

sealed interface A1 : A {

    val value_A1: Int
}

class A1A(val value_A1A: Int, override val value_A1: Int, override val value_A: Int) : A1
class A1B(val value_A1B: Int, override val value_A1: Int, override val value_A: Int) : A1

class A2(val value_A2: Int, override val value_A: Int) : A
