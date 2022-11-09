package `tests`.`default_arguments`.`classes`.`extensions`.`collision_detection`.`no_collision`.`different_class`

import tests.default_arguments.classes.extensions.collision_detection.A

fun A.foo(i: Int = zero, k: Int = 1): Int = i - k

class B

fun B.foo(i: Int): Int = i

