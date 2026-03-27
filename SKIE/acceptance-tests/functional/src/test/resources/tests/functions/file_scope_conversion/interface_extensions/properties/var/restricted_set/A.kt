package `tests`.`functions`.`file_scope_conversion`.`interface_extensions`.`properties`.`var`.`restricted_set`

import tests.functions.file_scope_conversion.interface_extensions.I

private var fooStorage = 1

var I.privateFoo: Int
    get() = fooStorage
    private set(value) {
        fooStorage = value - this.value
    }

var I.internalFoo: Int
    get() = fooStorage
    internal set(value) {
        fooStorage = value - this.value
    }
