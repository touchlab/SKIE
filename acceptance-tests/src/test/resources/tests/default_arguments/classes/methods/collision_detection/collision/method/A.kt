package `tests`.`default_arguments`.`classes`.`methods`.`collision_detection`.`collision`.`method`

class A {

    val zero: Int = 0

    fun foo(i: Int): Int = i + zero

    fun foo(i: Int = zero, k: Int = 1): Int = i - k
}
