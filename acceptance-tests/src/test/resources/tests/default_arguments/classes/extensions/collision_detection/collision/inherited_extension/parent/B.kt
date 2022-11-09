package `tests`.`default_arguments`.`classes`.`extensions`.`collision_detection`.`collision`.`inherited_extension`.`parent`

open class BaseB {

    val zero: Int = 0
}

class B : BaseB()

fun BaseB.foo(i: Int = zero, k: Int = 1): Int = i - k

fun B.foo(i: Int): Int = i + zero
