package `tests`.`default_arguments`.`interfaces`.`extensions`.`collision_detection`.`no_collision`.`inherited_method`.`child`

interface I {

    val zero: Int

    fun foo(i: Int): Int = i + zero
}

interface B : I

class BImpl : B {

    override val zero: Int = 0
}

fun B.foo(i: Int = zero, k: Int = 1): Int = i - k
