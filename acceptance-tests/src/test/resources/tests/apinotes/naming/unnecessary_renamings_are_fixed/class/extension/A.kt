package `tests`.`apinotes`.`naming`.`unnecessary_renamings_are_fixed`.`class`.`extension`

class A

fun A.foo(i: Int) = i

fun A.foo(i: String) = i.toInt()
