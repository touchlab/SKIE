package `tests`.`default_arguments`.`classes`.`extensions`.`collision_detection`.`collision`.`generic_class`

class B<T: Int> {

    val zero: Int = 0

    fun foo(i: T): Int = i + zero
}

fun B<*>.foo(i: Int = zero, k: Int = 1): Int = i - k
