package `tests`.`default_arguments`.`interfaces`.`extensions`.`collision_detection`.`collision`.`extension`

import tests.default_arguments.interfaces.extensions.collision_detection.A

fun A.foo(i: Int = zero, k: Int = 1): Int = i - k

fun A.foo(i: Int): Int = i + zero
