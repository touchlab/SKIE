package `tests`.`coroutines`.`suspend`.`interfaces`.`methods`.`visibility`.`internal`

internal interface A {

    suspend fun foo(): Int = 0
}

class A1 : A
