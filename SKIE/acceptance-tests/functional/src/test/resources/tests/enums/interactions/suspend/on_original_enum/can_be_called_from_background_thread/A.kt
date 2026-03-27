package `tests`.`enums`.`interactions`.`suspend`.`on_original_enum`.`can_be_called_from_background_thread`

enum class A {
    A1, A2;

    suspend fun foo(): Int = 0
}
