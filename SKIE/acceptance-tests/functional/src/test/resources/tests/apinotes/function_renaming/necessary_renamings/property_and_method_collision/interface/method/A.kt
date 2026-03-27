package `tests`.`apinotes`.`function_renaming`.`necessary_renamings`.`property_and_method_collision`.`interface`.`method`

interface A {

    val foo: Int
        get() = 0

    fun foo() = 0
}

class A1 : A
