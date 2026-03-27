package `tests`.`default_arguments`.`interfaces`.`extensions`.`collision_detection`.`no_collision`.`same_function_name_different_parameter_name`

import tests.default_arguments.interfaces.extensions.collision_detection.A

fun A.foo(i: Int = zero, k: Int = 1): Int = i - k

fun A.foo(m: Int): Int = m + zero
