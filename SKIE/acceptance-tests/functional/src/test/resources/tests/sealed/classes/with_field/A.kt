package `tests`.`sealed`.`classes`.`with_field`

sealed class A(val value_A: Int)

class A1(val value_A1: Int, value_A: Int) : A(value_A)
class A2(val value_A2: Int, value_A: Int) : A(value_A)
