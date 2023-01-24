package `tests`.`apinotes`.`naming`.`unnecessary_renamings_are_fixed`.`class`.`extension_and_method`

class A {

    fun foo(i: Int) = i
}

fun A.foo(i: String) = i.toInt()
