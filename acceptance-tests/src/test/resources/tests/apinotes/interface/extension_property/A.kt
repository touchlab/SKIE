package `tests`.`apinotes`.`interface`.`extension_property`

import tests.apinotes.naming.necessary_renamings.property_and_method_collision.`class`.extension.A

interface A

class A1 : A

val A.foo: Int
    get() = 0
