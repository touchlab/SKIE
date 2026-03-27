package `tests`.`enums`.`interactions`.`suspend`.`callback_version_is_preserved`

enum class A {
    A1, A2;

    suspend fun foo(): Int = 0
}
