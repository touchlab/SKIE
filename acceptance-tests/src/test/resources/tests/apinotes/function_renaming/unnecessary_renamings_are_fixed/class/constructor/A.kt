package `tests`.`apinotes`.`function_renaming`.`unnecessary_renamings_are_fixed`.`class`.`constructor`

class A {

    val value: Int

    constructor(i: Int) {
        value = i
    }

    constructor(i: String) {
        value = i.toInt()
    }
}

