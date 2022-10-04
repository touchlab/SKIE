package `tests`.`default_arguments`.`classes`.`methods`.`multiple_parameters`.`different_types`

class A {

    fun foo(i: Double, k: Int = 1, m: Int, o: Double = 3.0): Double = i + (k - m) * o
}