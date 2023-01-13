package `tests`.`apinotes`.`nested_inside_non_exported`.`single`

class A(delegate: List<Int>) : List<Int> by delegate {

    companion object
}

fun A.Companion.foo(): Int = 0
