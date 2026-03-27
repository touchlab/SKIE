package `tests`.`default_arguments`.`global_functions`.`collision_detection`.`no_collision`.`same_names_different_parameter_type`

fun foo(i: Int = 0, k: Int = 1): Int = i - k

fun foo(i: String): Int = 0
