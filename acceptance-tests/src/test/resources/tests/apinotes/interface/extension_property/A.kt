package `tests`.`apinotes`.`interface`.`extension_property`

interface A

class A1 : A

private var shared: Int = 10

var A.foo: Int
    get() = shared
    set(value) {
        shared = value
    }


