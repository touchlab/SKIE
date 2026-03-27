package `tests`.`apinotes`.`function_renaming`.`necessary_renamings`.`value_class_as_parameter`.`constructor`

@JvmInline
value class V(val value: Int)

class A {

    val value: Int

    constructor(a: Int, i: V) {
        value = i.value + 1
    }

    constructor(a: Int, i: Int) {
        value = i
    }
}

