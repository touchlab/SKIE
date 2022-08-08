package tests.sealed.combined.multiple_interfaces

sealed interface I {

    val value_I: Int
}

sealed interface K {

    val value_K: Int
}

class A1(val value_A1: Int, override val value_I: Int, override val value_K: Int) : I, K
class A2(val value_A2: Int, override val value_I: Int) : I
class A3(val value_A2: Int, override val value_K: Int) : K