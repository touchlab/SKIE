package `tests`.`default_arguments`.`classes`.`methods`.`collision_detection`.`no_collision`.`same_names_different_parameter_type`

class A {

    val zero = 0

    fun foo(i: Int = zero, k: Int = 1): Int = i - k

    fun foo(i: String): Int = zero
}
