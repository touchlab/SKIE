package `tests`.`other`.`name_collision_warnings`.`suppressed`

import co.touchlab.skie.configuration.annotations.SuppressSkieWarning

fun foo(i: Int) {
}

@SuppressSkieWarning.NameCollision
fun foo(i: A) {
}

value class A(val i: Int)
