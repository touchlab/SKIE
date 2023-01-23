package `tests`.`apinotes`.`naming`.`necessary_renamings`.`property_and_method_collision`.`inheritance`.`method_in_interface`

interface A {

    fun foo() = 0
}

class A1 : A {

    val foo: Int
        get() = 0
}
