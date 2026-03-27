package `tests`.`default_arguments`.`classes`.`extensions`.`collision_detection`.`collision`.`inherited_method`.`child`

open class BaseB {

    val zero: Int = 0

    fun foo(i: Int): Int = i + zero
}

interface I {

    fun foo(k: Double): Int = k.toInt() - 1
}

class B : BaseB(), I

fun B.foo(i: Int = zero, k: Double = 1.0): Int = i - k.toInt()
