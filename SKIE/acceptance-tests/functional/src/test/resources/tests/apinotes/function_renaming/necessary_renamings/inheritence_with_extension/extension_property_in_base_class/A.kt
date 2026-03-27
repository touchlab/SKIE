package `tests`.`apinotes`.`function_renaming`.`necessary_renamings`.`inheritence_with_extension`.`extension_property_in_base_class`

open class A

class A1 : A() {

    var foo: Int = 0
}

var shared = 1

var A.foo: Int
    get() = shared
    set(value) {
        shared = value
    }
