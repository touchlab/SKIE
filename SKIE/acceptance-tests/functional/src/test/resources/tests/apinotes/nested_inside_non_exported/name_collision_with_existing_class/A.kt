package `tests`.`apinotes`.`nested_inside_non_exported`.`name_collision_with_existing_class`

class A(delegate: List<Int>) : List<Int> by delegate {

    class B
}

class AB {

    fun foo(): Int = 1
}

class AB_ {

}

fun A.B.foo(): Int = 0
