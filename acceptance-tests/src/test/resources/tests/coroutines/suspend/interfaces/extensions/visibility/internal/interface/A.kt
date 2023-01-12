package `tests`.`coroutines`.`suspend`.`interfaces`.`extensions`.`visibility`.`internal`.`interface`

internal interface A

class A1 : A

internal suspend fun A.foo(): Int = 0
