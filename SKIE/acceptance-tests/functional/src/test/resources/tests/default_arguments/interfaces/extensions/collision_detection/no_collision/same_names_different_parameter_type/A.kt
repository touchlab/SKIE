package `tests`.`default_arguments`.`interfaces`.`extensions`.`collision_detection`.`no_collision`.`same_names_different_parameter_type`

import tests.default_arguments.interfaces.extensions.collision_detection.A

fun A.foo(i: Int = zero, k: Int = 1): Int = i - k

fun A.foo(i: String): Int = zero
