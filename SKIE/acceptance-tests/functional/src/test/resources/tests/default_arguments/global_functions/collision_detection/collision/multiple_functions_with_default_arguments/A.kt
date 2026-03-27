package `tests`.`default_arguments`.`global_functions`.`collision_detection`.`collision`.`multiple_functions_with_default_arguments`

fun foo(i: Int = 0, k: Int = 1): Int = i - k

fun foo(i: Int = 0, m: Double = 2.0): Int = i + m.toInt()
