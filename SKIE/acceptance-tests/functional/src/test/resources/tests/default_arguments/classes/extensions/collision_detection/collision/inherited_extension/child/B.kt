package `tests`.`default_arguments`.`classes`.`extensions`.`collision_detection`.`collision`.`inherited_extension`.`child`

open class BaseB {

    val zero: Int = 0
}

class B : BaseB()

fun BaseB.foo(i: Int): Int = i + zero

fun B.foo(i: Int = zero, k: Int = 1): Int = i - k
