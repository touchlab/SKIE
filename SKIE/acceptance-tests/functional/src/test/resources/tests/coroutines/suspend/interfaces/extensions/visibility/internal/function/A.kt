package `tests`.`coroutines`.`suspend`.`interfaces`.`extensions`.`visibility`.`internal`.`function`

interface A

class A1 : A

internal suspend fun A.foo(): Int = 0

fun bar() {
}
