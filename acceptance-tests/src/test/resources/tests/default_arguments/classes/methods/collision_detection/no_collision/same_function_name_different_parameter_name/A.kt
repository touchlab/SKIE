package `tests`.`default_arguments`.`classes`.`methods`.`collision_detection`.`no_collision`.`same_function_name_different_parameter_name`

class A {

    val zero = 0

    fun foo(i: Int = zero, k: Int = 1): Int = i - k

    fun foo(m: Int): Int = m + zero
}
