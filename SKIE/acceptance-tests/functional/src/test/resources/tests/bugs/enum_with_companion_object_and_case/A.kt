package `tests`.`bugs`.`enum_with_companion_object_and_case`

enum class A {
    companion;

    companion object {

        val case: A = companion
    }
}
