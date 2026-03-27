package `tests`.`functions`.`file_scope_conversion`.`interface_extensions`.`properties`.`var`.`visibility`.`internal`.`kotlin`

import tests.functions.file_scope_conversion.interface_extensions.I

private var fooStorage = 1

internal var I.foo: Int
    get() = fooStorage
    set(value) {
        fooStorage = value - this.value
    }
