package `tests`.`apinotes`.`naming`.`unnecessary_renamings_are_fixed`.`multiple_parameters`

fun foo(a: Int, i: Int) = i

fun foo(a: Int, i: String) = i.toInt()
