package `tests`.`default_arguments`.`global_functions`.`vararg`.`with_default_value`

fun foo(i: Int = 0, vararg k: Int = intArrayOf(1)): Int = i + k.sum()
