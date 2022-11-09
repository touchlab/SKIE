package `tests`.`default_arguments`.`classes`.`extensions`.`collision_detection`.`collision`.`method`

class B {

    val zero: Int = 0

    fun foo(i: Int): Int = i + zero
}

fun B.foo(i: Int = zero, k: Int = 1): Int = i - k
