package `tests`.`default_arguments`.`classes`.`methods`.`collision_detection`.`no_collision`.`different_function_name`

class A {

    val zero = 0

    fun foo(i: Int = zero, k: Int = 1): Int = i - k

    fun bar(i: Int): Int = i + zero
}
