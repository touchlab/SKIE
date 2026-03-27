package `tests`.`bugs`.`spaces in path`

sealed interface A {
    object A1 : A {

        suspend fun foo(): Int = 0
    }
}
