package `tests`.`default_arguments`.`interfaces`.`extensions`.`collision_detection`.`no_collision`.`different_interface`

import tests.default_arguments.interfaces.extensions.collision_detection.A

fun A.foo(i: Int = zero, k: Int = 1): Int = i - k

interface B

fun B.foo(i: Int): Int = i

