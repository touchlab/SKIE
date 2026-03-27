package `tests`.`apinotes`.`function_renaming`.`necessary_renamings`.`property_and_method_collision`.`interface`.`extension`

interface A

val A.foo: Int
    get() = 0

fun A.foo() = 1

class A1 : A
