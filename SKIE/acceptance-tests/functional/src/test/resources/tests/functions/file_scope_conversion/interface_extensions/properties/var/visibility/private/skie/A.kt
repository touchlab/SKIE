package `tests`.`functions`.`file_scope_conversion`.`interface_extensions`.`properties`.`var`.`visibility`.`private`.`skie`

import co.touchlab.skie.configuration.annotations.SkieVisibility
import tests.functions.file_scope_conversion.interface_extensions.I

private var fooStorage = 1

@SkieVisibility.Private
var I.foo: Int
    get() = fooStorage
    set(value) {
        fooStorage = value - this.value
    }
