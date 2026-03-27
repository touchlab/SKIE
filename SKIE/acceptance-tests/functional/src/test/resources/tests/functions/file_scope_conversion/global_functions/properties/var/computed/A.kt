package `tests`.`functions`.`file_scope_conversion`.`global_functions`.`properties`.`var`.`computed`

private var fooStorage = 1

var foo: Int
    get() = fooStorage
    set(value) {
        fooStorage = value
    }
