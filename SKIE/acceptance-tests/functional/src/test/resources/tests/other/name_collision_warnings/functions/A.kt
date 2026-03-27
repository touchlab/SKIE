package `tests`.`other`.`name_collision_warnings`.`functions`

fun foo(i: Int) {
}

fun foo(i: A) {
}

value class A(val i: Int)
