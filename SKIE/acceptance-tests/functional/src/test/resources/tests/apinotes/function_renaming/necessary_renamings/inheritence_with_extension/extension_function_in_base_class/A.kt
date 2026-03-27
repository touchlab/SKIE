package `tests`.`apinotes`.`function_renaming`.`necessary_renamings`.`inheritence_with_extension`.`extension_function_in_base_class`

open class A

class A1 : A() {

    fun foo(): Int = 0
}

fun A.foo(): Int = 1
