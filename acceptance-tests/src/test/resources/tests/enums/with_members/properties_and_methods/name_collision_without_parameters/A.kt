package `tests`.`enums`.`with_members`.`properties_and_methods`.`name_collision_without_parameters`

enum class A {
    A1;

    val foo: Int = 0

    fun foo(): Int = 0
}
