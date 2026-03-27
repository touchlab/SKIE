package `tests`.`apinotes`.`function_renaming`.`necessary_renamings`.`property_and_method_collision`.`inheritance`.`property_in_interface`

interface A {

    val foo: Int
        get() = 0
}

class A1 : A {

    fun foo() = 0
}
