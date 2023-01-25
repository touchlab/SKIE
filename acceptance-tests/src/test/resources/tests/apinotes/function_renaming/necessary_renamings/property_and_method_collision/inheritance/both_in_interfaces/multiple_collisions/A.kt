package `tests`.`apinotes`.`function_renaming`.`necessary_renamings`.`property_and_method_collision`.`inheritance`.`both_in_interfaces`.`multiple_collisions`

interface A {

    fun foo() = 0
}

interface B {

    val foo: Int
        get() = 0
}

interface C {

    fun foo() = 0
}


class A1: A
class C1: C

class ABC : A, B, C {

    override fun foo(): Int = 0
}

