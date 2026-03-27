package `tests`.`functions`.`file_scope_conversion`.`interface_extensions`.`properties`.`var`.`regular`

import tests.functions.file_scope_conversion.interface_extensions.I

private var fooStorage = 1

var I.foo: Int
    get() = fooStorage
    set(value) {
        fooStorage = value - this.value
    }
