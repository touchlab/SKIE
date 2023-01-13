package `tests`.`default_arguments`.`classes`.`methods`.`multiple_functions`.`with_different_names`

class A {

    fun bar(i: Int, k: Int = 1): Int = i + k

    fun foo(i: Int = 0, k: Int): Int = i + k
}
