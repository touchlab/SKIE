package `tests`.`default_arguments`.`interfaces`.`extensions`.`collision_detection`.`no_collision`.`different_function_name`

import tests.default_arguments.interfaces.extensions.collision_detection.A

fun A.foo(i: Int = zero, k: Int = 1): Int = i - k

fun A.bar(i: Int): Int = i + zero
