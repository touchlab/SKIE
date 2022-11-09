package `tests`.`default_arguments`.`global_functions`.`collision_detection`.`collision`.`generic_function`

fun foo(i: Int = 0, k: Int = 1): Int = i - k

fun <T: Int> foo(i: T): Int = i
