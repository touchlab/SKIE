package `tests`.`apinotes`.`naming`.`necessary_renamings`.`property_and_method_collision`.`interface`.`extension`

interface A

fun A.foo(i: Int) = i

fun A.foo(i: String) = i.toInt()

class A1 : A
