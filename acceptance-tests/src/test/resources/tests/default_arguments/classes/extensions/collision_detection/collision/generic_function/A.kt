package `tests`.`default_arguments`.`classes`.`extensions`.`collision_detection`.`collision`.`generic_function`

import tests.default_arguments.classes.extensions.collision_detection.A

fun A.foo(i: Int = zero, k: Int = 1): Int = i - k

fun <T : Int> A.foo(i: T): Int = i + zero
