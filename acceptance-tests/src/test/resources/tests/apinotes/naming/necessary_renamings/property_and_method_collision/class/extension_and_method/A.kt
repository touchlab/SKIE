package `tests`.`apinotes`.`naming`.`necessary_renamings`.`property_and_method_collision`.`class`.`extension_and_method`

class A {

    fun foo() = 0
}

val A.foo: Int
    get() = 0
