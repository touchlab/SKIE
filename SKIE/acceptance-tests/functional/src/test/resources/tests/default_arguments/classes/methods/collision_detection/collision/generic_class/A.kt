package `tests`.`default_arguments`.`classes`.`methods`.`collision_detection`.`collision`.`generic_class`

class A<T : Int> {

    val zero: Int = 0

    fun foo(i: T): Int = i + zero

    fun foo(i: Int = zero, k: Int = 1): Int = i - k
}
