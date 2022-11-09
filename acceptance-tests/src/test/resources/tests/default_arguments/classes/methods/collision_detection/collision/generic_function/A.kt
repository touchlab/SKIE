package `tests`.`default_arguments`.`classes`.`methods`.`collision_detection`.`collision`.`generic_function`

class A {

    val zero = 0

    fun foo(i: Int = zero, k: Int = 1): Int = i - k

    fun <T : Int> foo(i: T): Int = i + zero
}
