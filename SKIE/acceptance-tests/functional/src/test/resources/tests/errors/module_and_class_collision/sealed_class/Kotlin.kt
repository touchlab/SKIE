package `tests`.`errors`.`module_and_class_collision`.`sealed_class`

sealed class Kotlin {

    class A1 : Kotlin()
    class A2 : Kotlin()
}
