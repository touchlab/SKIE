package `tests`.`apinotes`.`function_renaming`.`unnecessary_renamings_are_fixed`.`class`.`extension`

class A

fun A.foo(i: Int) = i

fun A.foo(i: String) = i.toInt()
