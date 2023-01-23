package `tests`.`apinotes`.`naming`.`necessary_renamings`.`property_and_method_collision`.`visibility`.`private`

class A {

    private val foo: Int
        get() = 0

    fun foo() = 0
}
