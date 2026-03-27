package `tests`.`default_arguments`.`classes`.`methods`.`with_extension_receiver`

class A {

    val zero = 0

    fun B.foo(i: Int = zero, k: Int = one): Int = i - k
}

class B {

    val one = 1
}
