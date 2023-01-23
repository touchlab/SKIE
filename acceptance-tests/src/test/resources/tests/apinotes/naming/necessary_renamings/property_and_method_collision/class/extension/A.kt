package `tests`.`apinotes`.`naming`.`necessary_renamings`.`property_and_method_collision`.`class`.`extension`

class A

val A.foo: Int
    get() = 0

fun A.foo() = 0
