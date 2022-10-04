package `tests`.`default_arguments`.`classes`.`methods`.`multiple_parameters`.`all_default_values`

class A {

    fun foo(i: Int = 0, k: Int = 1, m: Int = 2): Int = i + k * m - k
}