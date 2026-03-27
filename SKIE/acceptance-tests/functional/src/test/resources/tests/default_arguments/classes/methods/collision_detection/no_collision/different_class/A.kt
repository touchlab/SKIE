package `tests`.`default_arguments`.`classes`.`methods`.`collision_detection`.`no_collision`.`different_class`

class A {

    val zero = 0

    fun foo(i: Int = zero, k: Int = 1): Int = i - k
}

class B {

    fun foo(i: Int): Int = i
}

