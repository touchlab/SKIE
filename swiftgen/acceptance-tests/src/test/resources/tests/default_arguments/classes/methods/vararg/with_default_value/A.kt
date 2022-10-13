package `tests`.`default_arguments`.`classes`.`methods`.`vararg`.`with_default_value`

class A {

    fun foo(i: Int = 0, vararg k: Int = intArrayOf(1)): Int = i + k.sum()
}
