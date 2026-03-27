package `tests`.`functions`.`file_scope_conversion`.`interface_extensions`.`functions`.`visibility`.`internal`.`kotlin`

import tests.functions.file_scope_conversion.interface_extensions.I

internal fun I.foo(i: Int, k: Int): Int = i - k - value
