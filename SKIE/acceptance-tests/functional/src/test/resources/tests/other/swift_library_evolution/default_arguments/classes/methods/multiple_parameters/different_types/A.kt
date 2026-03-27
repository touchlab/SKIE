package `tests`.`other`.`swift_library_evolution`.`default_arguments`.`classes`.`methods`.`multiple_parameters`.`different_types`

class A {

    fun foo(i: Double, k: Int = 1, m: Int, o: Double = 3.0): Double = i + (k - m) * o
}
