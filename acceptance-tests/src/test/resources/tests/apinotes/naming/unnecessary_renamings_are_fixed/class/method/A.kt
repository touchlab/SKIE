package `tests`.`apinotes`.`naming`.`unnecessary_renamings_are_fixed`.`class`.`method`

class A {

    fun foo(i: Int) = i

    fun foo(i: String) = i.toInt()
}
