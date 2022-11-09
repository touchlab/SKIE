package `tests`.`default_arguments`.`interfaces`.`extensions`.`collision_detection`.`no_collision`.`inherited_method`.`parent`

interface I {

    val zero: Int
}

interface B : I {

    fun foo(i: Int): Int = i + zero
}

class BImpl : B {

    override val zero: Int = 0
}

fun I.foo(i: Int = zero, k: Int = 1): Int = i - k
