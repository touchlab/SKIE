package `tests`.`errors`.`module_and_class_collision`.`enum`

enum class Kotlin {
    A;
}

fun a(): Kotlin {
    return Kotlin.A
}
