package `tests`.`default_arguments`.`classes`.`methods`.`multiple_functions`.`with_different_parameters`

class A {

    fun foo(i: String = "", k: Int = 2) = k

    fun foo(a: Int = 1, b: Double = 0.0) = a
}
