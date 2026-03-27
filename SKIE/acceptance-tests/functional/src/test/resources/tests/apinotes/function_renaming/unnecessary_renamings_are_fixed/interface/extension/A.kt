package `tests`.`apinotes`.`function_renaming`.`unnecessary_renamings_are_fixed`.`interface`.`extension`

interface A

fun A.foo(i: Int) = i

fun A.foo(i: String) = i.toInt()

class A1 : A
