package `tests`.`apinotes`.`function_renaming`.`unnecessary_renamings_are_fixed`.`interface`.`method`

interface A {

    fun foo(i: Int) = i

    fun foo(i: String) = i.toInt()
}

class A1 : A
