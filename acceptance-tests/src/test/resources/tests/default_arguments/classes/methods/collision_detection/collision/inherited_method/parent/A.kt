package `tests`.`default_arguments`.`classes`.`methods`.`collision_detection`.`collision`.`inherited_method`.`parent`

open class BaseA {

    val zero: Int = 0

    fun foo(i: Int = zero, k: Int = 1): Int = i - k
}

class A : BaseA() {

    fun foo(i: Int): Int = i + zero
}
