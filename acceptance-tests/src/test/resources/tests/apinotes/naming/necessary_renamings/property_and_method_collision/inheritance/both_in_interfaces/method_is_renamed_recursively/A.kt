package `tests`.`apinotes`.`naming`.`necessary_renamings`.`property_and_method_collision`.`inheritance`.`both_in_interfaces`.`method_is_renamed_recursively`

interface A {

    fun foo() = 0
}

interface B {

    val foo: Int
        get() = 0
}

class A1: A

class AB : A, B

