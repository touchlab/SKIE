package tests.classes.multi_layer

sealed class A(val value_A: Int)

sealed class A1(val value_A1: Int, value_A: Int) : A(value_A)
class A1A(val value_A1A: Int, value_A1: Int, value_A: Int) : A1(value_A1, value_A)
class A1B(val value_A1B: Int, value_A1: Int, value_A: Int) : A1(value_A1, value_A)

class A2(val value_A2: Int, value_A: Int) : A(value_A)